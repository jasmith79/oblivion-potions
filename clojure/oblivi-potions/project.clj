(defproject oblivi-potions "0.1.0-SNAPSHOT"
  :description "Oblivion Potion Generator"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/math.combinatorics "0.1.1"]
                 [org.clojure/math.numeric-tower "0.0.4"]]
  :main ^:skip-aot oblivi-potions.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
