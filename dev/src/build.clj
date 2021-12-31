(ns build
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]))

(def lib 'study.sino/pinyin)

(def version (format "1.0.%s" (b/git-count-revs nil)))

(defn ci
  "Run the CI pipeline of tests (and build the JAR)."
  [opts]
  (-> opts
      (assoc :lib lib :version version)
      (bb/run-tests)
      (bb/clean)
      (bb/jar)))

(defn install
  "Install the JAR locally."
  [opts]
  (-> opts
      (assoc :lib lib :version version)
      (bb/install)))

(defn deploy
  "Deploy the JAR to Clojars."
  [opts]
  (-> opts
      (assoc :lib lib :version version)
      (bb/deploy)))

(comment
  (ci {})
  (install {})
  (deploy {})
  #_.)
