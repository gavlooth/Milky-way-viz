(ns milky-way.functions
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async])
  (:import (org.apache.commons.math3.analysis.function Log Tanh Tan Sin Cos Gaussian)
           (org.apache.commons.math3.transform  DftNormalization  FastFourierTransformer)
           (org.apache.commons.math3.util FastMath)
           (org.apache.commons.math3.util MathArrays)))

(set! *warn-on-reflection* true)


(let [data-folder (-> "config.edn" slurp read-string :data)]
  (def domains (file-seq (io/file data-folder "domains")))
  (def spiral-sets (file-seq (io/file data-folder "spiral-sets")))
  (def characteristic-functions (file-seq (io/file data-folder "characteristic-functions")))
  (def gaussian-2d-functions  (file-seq (io/file data-folder "2d-gaussian-functions"))))


(def PI (FastMath/PI))


(def model& (atom {:Domain nil}))


(def step 0.001)
(def start 0.001)
(def finish (- PI step))
(def opts {:A 800 :B 0.4 :N 5})
(def angles [0 (/ PI 2) PI (/ (* 3 PI) 2)])


(defn ppr-str [& input]
  (let [o (new java.io.StringWriter)]
    (binding [*out* o]
      (apply clojure.pprint/pprint input)
      (str o))))


(declare generate-gaussian)
(declare spiral)
(declare ring)


(def functions  {:Log  #(FastMath/log %)
                 :Tan #(FastMath/tan %)
                 :Tanh  #(FastMath/tan %)
                 :Sin #(FastMath/sin %)
                 :Cos #(FastMath/cos %)
                 :Sqrt #(FastMath/sqrt %)
                 :Gaussian generate-gaussian
                 :FFT (FastFourierTransformer. DftNormalization/STANDARD)
                 :Spiral spiral
                 :Ring ring
                 :Sec  #(/ -1 (FastMath/pow ^double (FastMath/cos %) 2))
                 :Sech #(/ -1 (FastMath/pow ^double (FastMath/cosh %) 2))
                 :**  #(FastMath/pow ^double % 2)
                 :*n   #(FastMath/pow ^double % ^double %2)
                 :convolve   #(MathArrays/convolve (long-array %) (long-array %2))})


(defn generate-gaussian
  ([] (generate-gaussian {}))
  ([{:keys [norm mean standard-deviation]
     :or {mean 0 standard-deviation 1}}]
   (let [gaussian (if norm (Gaussian. norm mean standard-deviation)
                      (Gaussian. mean standard-deviation))]
     (fn [x] (.value ^double gaussian  (double  x))))))


(defn generate-2d-gaussian
  ([] (generate-2d-gaussian {}))
  ([{:keys [standard-deviation A]
     :or {standard-deviation 100 mean 0 A 0} :as opts}]
   (let [sqrt-2 (FastMath/sqrt  2)
         gaussian-1d (generate-gaussian (assoc opts :standard-deviation  (/ standard-deviation sqrt-2)))]
     (fn [[x y]]
       (*  (gaussian-1d x)  (gaussian-1d y))))))


(let [fun  (:Log functions)]
  (defn log [x] (fun (double x))))


(let [fun  (:Tan functions)]
  (defn tan [x] (fun (double x))))


(let [fun  (:Tanh functions)]
  (defn tanh  [x] (fun  (double x))))


(let [fun  (:Cos functions)]
  (defn cos [x] (fun (double x))))


(let [fun  (:Sin functions)]
  (defn sin [x] (fun (double x))))


(let [fun  (:Sec functions)]
  (defn sec [x] (fun (double x))))


(let [fun  (:** functions)]
  (defn ** [x] (fun (double x))))


(let [fun  (:*n functions)]
  (defn *4 [x] (fun (double x) 4))
  (defn *n [x n] (fun (double x) n)))


(defn spiral
  ([x] (spiral {}))
  ([x  opts]
   (let [{:keys [A B N]  :or {A 1 B 1 N 1}} opts]
     (/ A (log   (* B (tan (/ x (* 2 N)))))))))


(defn anti-spiral
  ([x] (anti-spiral {}))
  ([x   opts]
   (let [{:keys [A B N]  :or {A 1 B 1 N 1}} opts]
     (/ (log   (* B (tan (/ x (* 2 N))))) A))))


(defn anti-spiral-derivative
  ([x] (anti-spiral-derivative x {}))
  ([x opts]
   (let [{:keys [A B N]  :or {A 1 B 1 N 1}} opts]
     (/ (* 2  N (sec (/ x (* 2 N))))
        (*  A (tan (/ x (* 2 N))))))))
;;Fix the constant values

(defn spiral-dirivative [x  opts]
  (/ (* -1  (anti-spiral-derivative x opts))
     (** (anti-spiral x opts))))


(defn  parametric-spiral
  [x  opts]
  (let [r  (spiral x opts)]
    [(* r (sin x)) (* r (cos x))]))


(defn parametric-spiral-velocity  [x opts]
  (let [r  (spiral x opts)
        r' (spiral-dirivative x opts)]
    [(+  (* r' (sin x))
         (* r  (cos x)))
     (-  (* r' (cos x))
         (* r  (sin x)))]))


(defn ortho-normalize-vector [[x y]]
  (when (or (not= 0 x) (not= 0 y))
    (let [x' (/ x  (*n (+ (** y) (** x)) 0.5))
          y' (/ y  (*n (+ (** y) (** x)) 0.5))]
      [(* -1 y') x'])))


(defn normal-vector
  ([x opts] (normal-vector x opts 1))
  ([x opts l]
   (let  [[a b] (parametric-spiral x opts)
          the-point (parametric-spiral-velocity  x opts)
          [w z]  (ortho-normalize-vector the-point)]
     [(+ a (* l  w)) (+ b (* l z))])))


(defn single-spiral-set [width density]
  (for [phi (range start finish step)]
    (for [ksi (range (* -1  (/ width 2)) (/ width 2) (/ 1 density))]
      (normal-vector  phi opts  ksi))))


(defn single-spiral-complement-set [outer inner density]
  (for [phi (range start finish step)]
    (for [ksi (concat  (range (* -1 outer) (* -1 inner)  (/ 1 density))
                       (range   inner  outer  (/ 1 density)))]
      (normal-vector  phi opts  ksi))))


(defn- rotate-spiral [θ the-spiral]
  (let [rotation-matrix [[(cos θ) (* -1 (sin θ))]
                         [(sin θ) (cos θ)]]
        [[a b] [c d]] rotation-matrix]
    (map (fn [x] (map (fn [[x y]] [(+ (* x  a) (* y b))
                                   (+ (* x  c) (* y d))]) x)) the-spiral)))


(defn multispiral-set  [angles width density]
  (let [the-spiral (single-spiral-set width density)]
    (mapcat #(rotate-spiral % the-spiral)  angles)))


(defn multispiral-complement-set  [angles inner outer density]
  (let [the-spiral (single-spiral-complement-set outer inner density)]
    (mapcat #(rotate-spiral % the-spiral)  angles)))


(defn  generate-characteristic  []
  (when-let [[the-set the-complement] (:Domain @model&)]
    (memoize
     (fn [x]
       (cond (some #(x) the-set) 1
             (some #(x) the-complement) 0
             :default "NaN")))))


(defn  generate-characteristic-graph  [file-name]
  {:pre [string? file-name]}
  (when-let [[the-set the-complement]  (:Domain @model&)]
    (async/thread
      (try
        (with-open [w (io/writer file-name)]
          (binding  [*out* w]
            (pr (vec (concat (map #(hash-map :x % :y 1) the-set)
                             (map #(hash-map :x % :y 0) the-complement))))))
        (.println System/out (str "gaussian was exported succesfuly"))
        (catch Exception e (.println System/out "Error loading Data" (.getMessage e)))))
    (.println System/out (str "generation of points started"))))


(defn generate-2d-gaussian-graph
  ([file-name] (generate-2d-gaussian-graph file-name {}))
  ([file-name opts]
   {:pre [string? file-name map? opts]}
   (when-let [[the-set the-complement]  (:Domain @model&)]
     (let [domain (vec (concat the-set the-complement))
           the-gaussian (generate-2d-gaussian opts)]
       (async/thread
         (try
           (with-open [w (io/writer file-name)]
             (binding  [*out* w]
               (pr (mapv (fn [x] (hash-map :x x :y (the-gaussian x))) domain))))
           (.println System/out (str "gaussian was exported succesfuly"))
           (catch Exception e (.println System/out "Error loading Data" (.getMessage e)))))
       (.println System/out (str "generation of points started"))))))


(defn  basic-spiral []
  (swap! model& assoc  :Domain
         [(vec (apply concat (multispiral-set angles 30 2)))
          (vec (apply concat  (multispiral-complement-set angles 15 100 2)))]))


(defn save-model  [file-name]
  {:pre [string? file-name]}
  (spit file-name  (ppr-str (-> model& deref :Domain))))


(defn load-model [file-name & {:keys [Key] :or {Key :Domain}}]
  (async/take!
   (async/thread
     (.println System/out (str "loading " (.getName file-name)))
     (read-string (slurp file-name)))
   (fn [x]
     (try
       (swap! model& assoc Key x)
       (.println System/out (str "data from " (.getName file-name) " was loaded succesfuly"))
       (catch Exception e (ppr-str "Error loading Data" (.getMessage e)))))))

(defn forward-transform [the-map]

  )


