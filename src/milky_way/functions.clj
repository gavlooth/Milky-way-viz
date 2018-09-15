(ns milky-way.functions
  (:require
    [infix.macros :refer [infix from-string]]
    [clojure.core.matrix :as matrix])
  (:import
    [org.apache.commons.math3.analysis.function Log Tanh Tan Sin Cos]
    [org.apache.commons.math3.util FastMath]))

(set! *warn-on-reflection* true)


(defmacro ->function [a-class]
 (let [the-class#  (vary-meta a-class assoc :tag (str a-class))]
     #(.value ~the-class# (double %))))

(def log (->function (Log.)))

(def tan  (->function (Tan.)))

(def tanh (->function (Tanh.)))

(def cos  (->function (Cos.)))

(def sin (->function (Sin.)))


(defn csc [x]
  (/ 1 (sin x)))


(defn  parametric-radius-spiral [A B N]
 (fn [phi]
   (infix A / log ( B * tan ( phi / (2 * N))))))


(defn    parametric-radius-ring [A B N]
 (fn [phi]
   (infix A / log ( B * tan ( phi / (2 * N))))))


(defn parametric-radius-spiral-derivative [A B N]
   (fn [phi]
    (infix phi -1 * A * csc (phi / N ) / (N * (log (B * tan(phi /(2 * N))) ** 2)))))


(defn spiral
  [x &  {:keys [A B N] :or {A 1 B 1 N 1}}]
  (let [r (parametric-radius-spiral A B N)]
    [(infix  r(x) *  sin(x)) (infix  r(x) * cos(x))]))


(defn spiral-galaxy-derivative
  [x &  {:keys [A B N] :or {A 1 B 1 N 1}}]
  (let [r ( parametric-radius-spiral A B N)
        r' (parametric-radius-spiral-derivative A B N)]
    [(infix r' sin(x) + r cos(x)) (infix r' cos(x) - r sin(x))]))

(defn fat-spiral [x fatness &  {:keys [A B N] :or {A 1 B 1 N 1}}])

(defn ring-galaxy
  [x &  {:keys [A B N] :or {A 1 B 1 N 1}}]
  (let [r      (/ A (log   (* B (tanh (/ x (* 2 N))))))]
    [(* r (sin x)) (* r (cos x))]))

