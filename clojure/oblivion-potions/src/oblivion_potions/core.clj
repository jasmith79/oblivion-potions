(ns oblivion-potions.core
  (:gen-class)
  (:require
   [clojure.data.json :as json]
   [oblivion-potions.csv :refer [parse-csv]]
   [oblivion-potions.utils :refer [combinations]]
   [oblivion-potions.potions :refer [create-ingredients create-potions format-potion best-by-effect]]
   [oblivion-potions.scoring :refer [default-scorer]]
   [oblivion-potions.combos :refer [p-combine print-short format-combo]]))

(defn now [] (.getTime (new java.util.Date)))

(def start (now))

(def min-potion-score 1300)

(def file-contents (slurp "../../allingredients.csv"))

(def ingredients (create-ingredients (parse-csv file-contents)))

;; (def _combos (combinations 4 (count ingredients)))

;; (def combos (take 2200000 _combos))
;; (def combos _combos)

(defn -main
  "Calculates potion combos"
  [& _args]
  (let [_ 1
        potions (json/read-str (slurp "1300.json"))
        ;; (create-potions ingredients default-scorer min-potion-score combos)
        best (best-by-effect potions)
        combos (p-combine best)]
    (json/pprint (sort-by #(% :score) (map format-combo combos)))))
  ;; (json/pprint (sort-by (fn [[_a _b {score :score}]] score) (combine potions)))))
  ;; (doseq [cbo (map print-short (combine potions))] (json/pprint cbo))))
  ;; (json/pprint (sort-by #(:score %) (map (partial format-potion ingredients) potions)))))
  ;; (json/pprint (count (best-by-effect potions)))))
  ;; (doseq [potion (create-potions ingredients default-scorer min-potion-score combos)] 
  ;;   (println (format-potion ingredients potion)))
  ;; (println "done in " (quot (- (now) start) 1000) " seconds."))
