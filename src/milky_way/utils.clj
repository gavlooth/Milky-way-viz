(ns milky-way.utils
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.core.async :as async]
            [taoensso.timbre :as timbre :refer [spy]])
  (:import (java.io BufferedReader PushbackReader FileReader IOException)
           (org.apache.commons.lang3 StringUtils)))


(def ->?double
  (s/conformer
   (fn [x]
     (cond
       (integer? x) x
       (string? x)  (try
                      (Double/parseDouble x)
                      (catch Exception _
                        :clojure.spec.alpha/invalid))
       :else :clojure.spec.alpha/invalid))))


(def ->?integer
  (s/conformer
   (fn  [x]
     (cond
       (integer? x) x
       (string? x)  (try
                      (Integer/parseInt (StringUtils/removeEnd  x ".0"))
                      (catch Exception _
                        :clojure.spec.alpha/invalid))
       :else :clojure.spec.alpha/invalid))))


(defn char-seq
  [^java.io.BufferedReader rdr]
  (let [chr (.read rdr)]
    (when-not (= chr -1)
      (cons (char chr) (lazy-seq  (char-seq rdr))))))

(defn file-print [file-name output & {:keys [append]} ]
  (try
    (with-open [w (io/writer (io/file  file-name) :append append)]
      (binding  [*out* w]
        (timbre/info  (str  "Writting file " file-name))
        (pr output ))
      (timbre/info (str  "Finished writting " file-name)))
    (catch Exception  e (timbre/info (str "error: " (.getMessage e))))))

(def csv-test "data/FL_insurance_sample.csv")

(defn chunked-line-seq [file-name & {:keys [chunck-size] :or {chunck-size 1000}}]
    (let [rd (io/reader (io/input-stream (io/file file-name)))
          chunck-lines  (fn [ ]
                          (seq (loop [lines (transient  [] ) counter 0]
                                 (if-let [line (.readLine rd)]
                                   (if (< counter chunck-size)
                                     (do
                                       (conj! lines line)
                                       (recur lines (inc counter)))
                                     (persistent! lines))
                                   (do (.close ^java.io.BufferedReader rd)
                                       (persistent! lines))))))]
      (letfn [(chuncked-seq [] (lazy-cat (chunck-lines) (chuncked-seq)))]
              (chuncked-seq))))
