(defproject milky-way "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [mount "0.1.11"]
                 [rm-hull/infix "0.3.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [quil "2.6.0"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.apache.commons/commons-math3 "3.6.1"]]
  :repl-options {:timeout  120000}
  :jvm-opts ["-Xmx6G"])


