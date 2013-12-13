(ns osc-control.core
  (:use
   overtone.live))

(comment

  ;; Start OSC client
  ;; Then you can load PD's osc-receiver patch
  (def client (osc-client "localhost" 9999))

  ;; A function to send new values to PD.
  ;; The Granita is the name of granulator synth patch
  ;; courtesy of Lorenzo Sutton
  ;; http://gitorious.org/granita/granita/trees/master
  (defn granita [pos-start pos-end] (do
                                      (osc-send client "/granita1/pos/start" (float pos-start))
                                      (osc-send client "/granita1/pos/end" (float pos-end))))


  ;; Some presets
  (granita 0.01 0.02)
  (granita 0.0 0.01)
  (granita 0.5 0.8)
  (granita 0.9 0.92)  ; ambience
  (granita 0.9 0.94)  ; more anxious ambience
  (granita 0.87 0.92) ; ambience with nice voice

  ;; Some stored presets
  (def ambience [0.9 0.92])
  (def anxious [0.9 0.94])
  (def voice [0.87 0.92])

  ;; Let's play them
  (apply granita ambience)
  (apply granita anxious)


  ;; The code below allows triggering a function on Overtone's control value change

  ;; create new id
  (def uid (trig-id))

  ;; define a synth which uses send-trig to pass line value from 0 to 1 in 1 minute
  (defsynth foo
    [t-id 0]
    (send-trig (impulse 10) t-id (line:kr 0 1 60 FREE)))


  ;; A function which interpolates between Granita's presets
  (defn passage [s1 s2] (fn [v]
                          (let
                              [ns (map #(+ %1( * v (- %2 %1))) s1 s2)]
                            (apply granita ns)
                            )))

  ;; Three nice transitions
  (def intro (passage ambience anxious))
  (def to-voice (passage ambience voice))
  (def from-voice (passage voice ambience))

  ;; A function to play a transition
  (defn play-passage [p]
    ;; register a handler fn
    (on-trigger uid
                (fn [val] (p val))
                ::granita1)
    ;; create a new instance of synth foo with trigger id as a
    ;; param
    (foo uid))

  ;; Some music!
  (play-passage from-voice)
  (play-passage intro)
  (play-passage to-voice)



  (stop)

  ;;Trigger handler can be removed with:
  (remove-event-handler ::granita1)
  (osc-close client)

  )
