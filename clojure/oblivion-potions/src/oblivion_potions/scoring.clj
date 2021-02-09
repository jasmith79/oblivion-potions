(ns oblivion-potions.scoring
  (:require
   [oblivion-potions.utils :refer [map-with-index-value]]
   [oblivion-potions.potions :refer [positives]]))

;; kv the lazy way { "Reflect Damage" 25 "Reflect Spell" 24 }
;; This is my priority list, YMMV. If you consider certain effects
;; better or worse than were I've placed them just alter the order
;; in the vec. Alternatively you can directly define a map with
;; weights {"Reflect Damage" 500 "Reflect Spell" 275 "Fortify Personality" 10}
(def positive-weights
  (map-with-index-value (reverse positives)))

(def negative-weights {"Paralysis"           1000
                       "Burden"              10
                       "Silence"             500
                       "Drain Health"        5
                       "Drain Fatigue"       5
                       "Drain Agility"       50
                       "Drain Speed"         50
                       "Drain Magicka"       5
                       "Drain Intelligence"  50
                       "Frost Damage"        5
                       "Shock Damage"        5
                       "Fire Damage"         5
                       "Damage Health"       5
                       "Damage Fatigue"      5
                       "Damage Magicka"      5
                       "Damage Endurance"    300
                       "Damage Speed"        200
                       "Damage Willpower"    100
                       "Damage Intelligence" 100
                       "Damage Personality"  100
                       "Damage Agility"      200
                       "Damage Luck"         100
                       "Damage Strength"     300})

(defn create-scoring-fn
  [positive-weights negative-weights]
  (fn [[negative-effects positive-effects]]
    (- (reduce + (map (fn [eff] (let [wt (positive-weights eff 0)] (* wt wt))) positive-effects))
       (reduce + (map #(negative-weights % 0) negative-effects)))))

(def default-scorer (create-scoring-fn positive-weights negative-weights))
