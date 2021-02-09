(ns oblivion-potions.core
  (:gen-class)
  (:require 
   [oblivion-potions.csv :refer [parse-csv]]
   [oblivion-potions.utils :refer [combinations]]
   [oblivion-potions.potions :refer [create-ingredients create-potions format-potion]]
   [oblivion-potions.scoring :refer [default-scorer]]))

(defn now [] (.getTime (new java.util.Date)))

(def start (now))

(def min-potion-score 3000)

(def file-contents (slurp "../../allingredients.csv"))

(def ingredients (create-ingredients (parse-csv file-contents)))

(def _combos (combinations 4 (count ingredients)))

(def combos (take 1000000 _combos))

(defn -main
  "Calculates potion combos"
  [& _args]
  (doseq [potion (create-potions ingredients default-scorer min-potion-score combos)] 
    (println (format-potion ingredients potion)))
  (println "done in " (quot (- (now) start) 1000) " seconds."))

;; (ns oblivion-potions.core
;;   (:gen-class)
;;   (:require
;;    [clojure.string :refer [includes? split join]]
;;    [clojure.set :refer [union subset?]]
;;    [clojure.tools.namespace.repl :refer [refresh refresh-all]]
;;    [clojure.data.json :as json]))

;; ;;;; Constants ;;;;
;; (defn now [] (.getTime (new java.util.Date)))
;; (def start (now))

;; ;;; Copied from rosetta code
;; (defn combinations
;;   "If m=1, generate a nested list of numbers [0,n)
;;    If m>1, for each x in [0,n), and for each list in the recursion on [x+1,n), cons the two"
;;   [m n]
;;   (letfn [(comb-aux
;;             [m start]
;;             (if (= 1 m)
;;               (for [x (range start n)]
;;                 (list x))
;;               (for [x (range start n)
;;                     xs (comb-aux (dec m) (inc x))]
;;                 (cons x xs))))]
;;     (comb-aux m 0)))

;; (def dupes (atom #{}))
;; (defn already-have-effects?
;;   [effects]
;;   (let [existing @dupes]
;;     (or (contains? existing effects)
;;         (some (fn [existing-effs] (subset? effects existing-effs)) existing))))

;; ;; (defn add-effects [effs] (swap! dupes conj effs))
;; (defn add-effects
;;   [effs]
;;   (swap! dupes
;;          (fn
;;            [prev current]
;;            (conj (into #{}
;;                        (filter (fn [item] (not (subset? item current)))
;;                                prev))
;;                  current))
;;          effs))

;; (def potions-kept (atom {}))
;; (defn potion-swapper
;;   [existing-map potion]
;;   (let [existing-keys (set (keys existing-map))
;;         current-effects (potion :effects)]
;;     ; if the effects of the current potion are already present in the map
;;     ; we can stop
;;     (if (contains? existing-keys current-effects)
;;       existing-map
;;       ; check if the current potion is strictly better than one of the existing
;;       ; potions we already have
;;       (let [subs (filter (fn [x] (subset? x current-effects)) existing-keys)]
;;         (if (empty? subs)
;;           ; add the new potion
;;           (conj existing-map [current-effects potion])
;;           ; replace the old potion with the strictly better one
;;           (assoc (dissoc existing-map (first subs)) current-effects potion))))))

;; (defn add-potion
;;   [potion]
;;   (swap! potions-kept potion-swapper potion))

;; ;; kv the lazy way { "Reflect Damage" 25 "Reflect Spell" 24 }
;; (def priority
;;   (into {}
;;         (map-indexed (fn [idx, itm] [itm, (+ 1 idx)])
;;                      (reverse ["Reflect Damage"
;;                                "Reflect Spell"
;;                                "Fortify Health"
;;                                "Fortify Endurance"
;;                                "Fortify Magicka"
;;                                "Fortify Strength"
;;                                "Fortify Speed"
;;                                "Restore Health"
;;                                "Restore Magicka"
;;                                "Shock Shield"
;;                                "Shield"
;;                                "Fortify Agility"
;;                                "Fortify Intelligence"
;;                                "Fortify Willpower"
;;                                "Fortify Fatigue"
;;                                "Resist Magicka"
;;                                "Spell Absorption"
;;                                "Fire Shield"
;;                                "Frost Shield"
;;                                "Restore Endurance"
;;                                "Light"
;;                                "Restore Fatigue"
;;                                "Invisibility"
;;                                "Fortify Luck"
;;                                "Chameleon"
;;                                "Fortify Personality"]))))

;; (def MIN_POTION_SCORE 1500)
;; (def MIN_COMBO_SCORE 3000)

;; (def negatives ["Damage"
;;                 "Drain"
;;                 "Paralyze"
;;                 "Burden"
;;                 "Silence"])

;; (defn negative?
;;   [eff]
;;   (some (fn [ng] (includes? eff ng)) negatives))

;; (defn calc-effects
;;   "Takes a seq of all the effects from all the ingredients in a 4 ingredient combo
;;    and returns a set of the matches."
;;   [effects]
;;   (set (->> effects
;;             (frequencies)
;;             (filter (fn [[_k v]] (> v 1)))
;;             (keys))))

;; (defn calc-score
;;   [effs]
;;   (reduce (fn
;;             [score eff]
;;             (let [eff-score (priority eff)]
;;               (if (nil? eff-score) score (+ score (* eff-score eff-score)))))
;;           0
;;           effs))

;; ;; I'm not pulling in a dep just to parse a csv
;; (defn split-lines
;;   [txt]
;;   (split txt #"\n"))

;; (defn split-row
;;   [row]
;;   (split row #",\s?"))

;; (defn empty-line?
;;   [line]
;;   ;; clojure is smart enough here to discard the empty lines, so no need to check the length
;;   ;; like javascript, however needs the unicode handling for some reason.
;;   (not (re-find #"(?u),[ \t\xA0\u1680\u180e\u2000-\u200a\u202f\u205f\u3000]*," line)))

;; (println "Reading file...")
;; (def file-contents (slurp "../../allingredients.csv"))
;; ;; (def file-contents (join "\n" ["Watcher's EyeSI,Restore Intelligence,Fortify Magicka,Light,Reflect Spell"
;; ;;                                "Withering MoonSI,Restore Magicka,Shield,Cure Disease,Reflect Spell"
;; ;;                                "Bog Beacon Asco Cap,Restore Magicka,Shield,Damage Personality,Damage Endurance"
;; ;;                                "Columbine Root Pulp,Restore Personality,Resist Frost,Fortify Magicka,Chameleon"
;; ;;                                "Elytra IchorSI,Restore Magicka,Burden,Chameleon,Silence"
;; ;;                                "Hunger TongueSI,Cure Poison,Cure Disease,Fire Damage,Fortify Magicka"
;; ;;                                "Red Kelp Gas BladderSI,Restore Speed,Water Breathing,Cure Disease,Fortify Magicka"
;; ;;                                "Boar Meat,Restore Health,Damage Speed,Fortify Health,Burden"
;; ;;                                "Ironwood Nut,Restore Intelligence,Resist Fire,Damage Fatigue,Fortify Health"
;; ;;                                "Lady's Smock Leaves,Restore Intelligence,Resist Fire,Damage Fatigue,Fortify Health"
;; ;;                                "Purgeblood SaltsVL,Restore Magicka,Damage Health,Fortify Health,Dispel"
;; ;;                                "Arrowroot,Restore Agility,Damage Luck,Fortify Strength,Burden"
;; ;;                                "Blackberry,Restore Fatigue,Resist Shock,Fortify Endurance,Restore Magicka"
;; ;;                                "Bloodgrass,Chameleon,Resist Paralysis,Burden,Fortify Health"
;; ;;                                "Boar Meat,Restore Health,Damage Speed,Fortify Health,Burden"
;; ;;                                "Void EssenceSI,Restore Health,Fortify Health,Fortify Strength,Fortify Endurance"]))

;; (def ingredients
;;   (map (fn
;;          [line]
;;          (let [[ing & effs] (split-row line)] {:name ing :effects effs}))
;;        (filter empty-line? (split-lines file-contents))))

;; (defn gen-potion
;;   [combo]
;;   (let [ingreds (map (fn [n] (nth ingredients n)) combo)
;;         effs (calc-effects (flatten (map (fn [ingred] (ingred :effects)) ingreds)))]
;;     (if (empty? effs)
;;       nil
;;       (let [score (calc-score effs)]
;;         (if (> score MIN_POTION_SCORE)
;;           (if (already-have-effects? effs)
;;             nil
;;             (do (add-effects effs)
;;                 {:ingredients (map (fn [ingred] (ingred :name)) ingreds)
;;                  :effects effs
;;                  :score score}))
;;           nil)))))

;; (println "Generating combinations...")
;; (def combos (combinations 4 (count ingredients)))

;; (println "Defining potions")

;; (doseq [potion (filter (fn [x] (not (nil? x)))
;;                        (map gen-potion combos))]
;;   (add-potion potion))

;; (defn -main
;;   "Calculates potion combos"
;;   [& _args]
;;   ; (println (first ingredients))
;;   ; (println (first combos))
;;   ; (doseq [effs (keys @potions-kept)] (println effs))
;;   ; (doseq [potion (vals @potions-kept)] (println potion))
;;   ; (doseq [potion potions] (println (potion :effects)))
;;   ; (println (count potions))
;;   ; (println "done in " (quot (- (now) start) 1000) " seconds."))
;;   nil)