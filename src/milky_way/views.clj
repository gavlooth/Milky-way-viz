(ns milky-way.views
  (:require
   [milky-way.functions :refer [spiral t-opts spiral-2d]]
   [quil.core :as q]
   [quil.middleware :as m])
  (:import (org.apache.commons.math3.util FastMath)))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(def step 0.001)

(def start 0.001)

(def finish (- FastMath/PI step))



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


(defn draw []
 (let [the-spiral   (spiral {:A 800 :B 0.4 :N 16})]
  ; move origin point to centre of the sketch
  ; by default origin is in the left top corner
  (q/background 231 5 100)
  (q/with-translation [(/ (q/width) 2) (/ (q/height) 2)]
    (q/line  -800 0 800 0)
    (q/line  0 -800 0 800)
    (doseq [phi (range start finish step)]
      (let [[x y] (the-spiral phi)]
        (q/point  x y)
        (q/point  (* -1 x) (* -1 y))
        (q/point  (* 1 y) (* -1 x))
        (q/point  (* -1 y) (* 1 x))))
    #_(doseq [phi (range start (* finish 0.5) step)
              :let [[x y] (the-spiral phi)]]
        (do
          (q/point  (* 1 y) (* -1 x))
          (q/point  (* -1 y) (* 1 x))))))
 (q/save  (str (uuid) "-milky-way.png")))

(defn draw-2d []
 (let [the-spiral   (spiral-2d {:A 800 :B 0.4 :N 16 :Width 8})]
  ; move origin point to centre of the sketch
  ; by default origin is in the left top corner
  (q/background 231 5 100)
  (q/with-translation [(/ (q/width) 2) (/ (q/height) 2)]
    (q/line  -800 0 800 0)
    (q/line  0 -800 0 800)
    (doseq [phi  (concat (range start finish step)
                         (map (partial * -10) (range start finish step)))
            t (range 0 0.5 0.1)]
      (let [[x y] (the-spiral phi t)]
        (q/point  x y)
        (q/point  (* -1 x) (* -1 y))
        (q/point  (* 1 y) (* -1 x))
        (q/point  (* -1 y) (* 1 x))))
   #_(q/save  (str (uuid) "-milky-way.png")))))





