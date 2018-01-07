(ns milky-way.views
  (:import [org.apache.commons.math3.util FastMath])
  (:require
   [milky-way.functions :as fns]
   [quil.core :as q]
   [quil.middleware :as m]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(def step 0.001)

(def start 0.001)

(def finish    (-   FastMath/PI step))


(defn draw-horizontal-line
  "Draws a horizontal line on the canvas at height h"
  [h]
  (q/line 10 h (- (q/width) 20) h)
  (q/stroke 255 h)
  (q/line 10 (+ h 4) (- (q/width) 20) (+ h 4)))

(defn draw-horizontal-line
  "Draws a vertical line on the canvas at width w"
  [w]
  (q/stroke 255 w))


(defn setup []
  (q/no-loop)
  (q/smooth)
  (q/stroke-weight 0.8)
  (q/color-mode :hsb 360 100 100 1.0))


; define function which draws spiral
(defn draw []
  ; move origin point to centre of the sketch
  ; by default origin is in the left top corner
  (q/background 231 20 90)
  (q/with-translation [(/ (q/width) 2) (/ (q/height) 2)]

    (q/line  -800 0 800 0)
    (q/line  0 -800 0 800)
    (doseq [phi (range start finish step)]
      (let [[x y] (fns/spiral-galaxy   phi  :A 800 :B 0.4 :N 16)]
        (q/point   x y)
        (q/point  (* -1 x) (* -1 y))))
    (doseq [phi (range start (/ finish  3) step)]
      (let [[x y] (fns/spiral-galaxy   phi  :A 800 :B 0.4 :N 16)]
        (q/point  (* 1 y) (* -1 x))
        (q/point  (* -1 y) (* 1 x)))))
  (q/save  (str (uuid) "-milky-way.png")))




