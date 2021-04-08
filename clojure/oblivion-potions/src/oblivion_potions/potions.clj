(ns oblivion-potions.potions
  (:require
   [clojure.string :refer [includes?]]
   [clojure.set :refer [subset?]]
   [oblivion-potions.utils :refer [partition-with]]
   [clojure.core.reducers :as r]))

(def positives ["Reflect Damage"
                "Reflect Spell"

                ; This up here
                ; because of rarity
                ; and not necessarily
                ; efficacy or desirability
                "Fortify Fatigue"

                ; Not actually present, but
                ; pads the scoring for the rare
                ; effects
                "Resist Magicka"
                "Spell Absorption"

                "Fortify Health"
                "Fortify Endurance"
                "Fortify Magicka"
                "Fortify Strength"
                "Fortify Speed"
                "Restore Health"
                "Restore Magicka"
                "Shock Shield"
                "Shield"
                "Fortify Agility"
                "Fortify Intelligence"
                "Fortify Willpower"
                "Fire Shield"
                "Frost Shield"
                "Restore Endurance"
                "Light"
                "Restore Fatigue"
                "Invisibility"
                "Fortify Luck"
                "Chameleon"
                "Fortify Personality"
                "Restore Strength"
                "Restore Agility"
                "Restore Intelligence"
                "Restore Luck"
                "Restore Speed"
                "Restore Willpower"
                "Restore Personality"])

(def positive-effects (into {} (map-indexed (comp vec reverse vector) positives)))

(def negatives ["Paralysis"
                "Burden"
                "Silence"
                "Drain Health"
                "Drain Fatigue"
                "Drain Agility"
                "Drain Speed"
                "Drain Magicka"
                "Drain Intelligence"
                "Frost Damage"
                "Shock Damage"
                "Fire Damage"
                "Damage Health"
                "Damage Fatigue"
                "Damage Magicka"
                "Damage Endurance"
                "Damage Speed"
                "Damage Willpower"
                "Damage Intelligence"
                "Damage Personality"
                "Damage Agility"
                "Damage Luck"
                "Damage Strength"])

(def negative-effects (into {} (map-indexed (comp vec reverse vector) negatives)))

(defn negative?
  "Predicate function that evaluates whether a given
   effect is undesirable."
  [eff]
  (contains? negative-effects eff))

(defn create-ingredient
  "Takes a sequence in the format:
   ingredient name, eff 1, eff 2, eff 3, eff 4 and
   returns a map with name and effects."
  [[name & effects]]
  {:name name
   :effects effects})

(defn create-ingredients
  "Takes a sequences of sequences in the format:
   ingredient name, eff 1, eff 2, eff 3, eff 4 and
   returns a map where the key is the position in
   the list and the value is a map with name and effects."
  [lines]
  (into {} (map-indexed (fn [idx line] [idx (create-ingredient line)]) lines)))

(defn calc-effects
  "Takes a list of ingredients and returns a vector containing a
   set of their combined negative effects and a set of their
   combined positive effects."
  [ingredients]
  (let [effects (flatten (map #(:effects %) ingredients))]
    (vec (map set
              (partition-with negative?
                              (->> effects
                                   (frequencies)
                                   (filter (fn [[_k v]] (> v 1)))
                                   (keys)))))))

(defn better?
  "Tests whether effects-list a is strictly better than 
   effects-list b: e.g. effects-list a's positive effects 
   are a superset of b's, whether effects-list a's negative
   effects are a subset of b's, etc."
  [[negative-a positive-a] [negative-b positive-b]]
  (when-not (and (= negative-a negative-b)
                 (= positive-a positive-b))
    (let [more-positive (and (subset? positive-b positive-a) (subset? negative-a negative-b))
          less-negative (and (= positive-b positive-a)
                             (not= negative-a negative-b)
                             (subset? negative-a negative-b))]
      ;; (println negative-a)
      ;; (println negative-b)
      ;; (println positive-a)
      ;; (println positive-b)
      ;; (println more-positive)
      ;; (println less-negative)
      (or more-positive less-negative))))

(defn poison?
  [[negative-effects positive-effects]]
  (and (empty? positive-effects) (seq negative-effects)))

(defn create-potion
  "Takes a sequence of ingredients, a scoring function, and a 4-tuple of
   ingredient indicies and returns a key/value tuple where the key is a
   2-tuple of the set of common negative effects and the set of common
   positive effects and the value is a map with the indgredient recipe
   and the score according to the provided scoring fn."
  [ingredients scoring-fn idxs]
  (let [effects (calc-effects (map #(ingredients %) idxs))]
    [effects
     {:ingredients idxs
      :score (scoring-fn effects)}]))

(defn filter-potions
  [potions potion]
  (let [[potion-effects] potion]
    (reduce (fn
              [[keepers keep-new] old-potion]
                (let [[old-effects] old-potion
                      old-is-better (better? old-effects potion-effects)
                      new-is-better (better? potion-effects old-effects)
                      still-keep-new (and keep-new (not old-is-better))
                      keep-old (and (not= old-effects potion-effects)
                                    (not new-is-better))]
                  [(if keep-old (conj keepers old-potion) keepers)
                  still-keep-new]))
            [[] true]
            potions)))

;; slower than the non/transient version using reduce
;; (defn filter-potions
;;   [potions potion]
;;   (let [[potion-effects] potion]
;;     (loop [i 0 
;;            keep-new true 
;;            keepers (transient [])]
;;       (let [old-potion (get potions i)]
;;         (if (nil? old-potion)
;;           [(persistent! keepers) keep-new]
;;           (let [[old-effects] old-potion
;;                 old-is-better (better? old-effects potion-effects)
;;                 new-is-better (better? potion-effects old-effects)
;;                 still-keep-new (and keep-new (not old-is-better))
;;                 keep-old (and (not= old-effects potion-effects)
;;                               (not new-is-better))]
;;             (recur (inc i) 
;;                    still-keep-new
;;                    (if keep-old (conj! keepers old-potion) keepers))))))))

(defn swapper
  "Takes a seq of potions and a new potion and conjs the new potion
   onto the result of dropping all potions from the existing list
   that are duplicates or strictly worse than the new potion."
  [potions potion]
  (let [[keepers keep-new] (filter-potions potions potion)]
    (if keep-new (conj keepers potion) keepers)))

(defn create-potions
  [ingredients scoring-fn threshold combinations]
  (let [potions (atom [])]
    (doseq [combo combinations]
      (let [potion (create-potion ingredients scoring-fn combo)
            [_effects {score :score}] potion]
        (when (> score threshold)
          (swap! potions swapper potion))))
     (deref potions)))

;; Slowest version?
;; (defn create-potions
;;   [ingredients scoring-fn threshold combinations]
;;   (reduce (fn 
;;             [keepers combo] 
;;             (let [potion (create-potion ingredients scoring-fn combo)
;;                   keep-new (atom true)
;;                   [potion-effects {score :score}] potion]
;;               (if (> score threshold)
;;                 (let [fltrd (reduce (fn 
;;                                       [acc old-potion]
;;                                       (let [[old-effects] old-potion
;;                                             old-is-better (better? old-effects potion-effects)
;;                                             new-is-better (better? potion-effects old-effects)
;;                                             keep-old (and (not= old-effects potion-effects)
;;                                                           (not new-is-better))]
;;                                         (if (and @keep-new old-is-better)
;;                                           (reset! keep-new false))
;;                                         (if keep-old
;;                                           (conj acc old-potion)
;;                                           acc))) 
;;                                     [] 
;;                                     keepers)]
;;                   (if @keep-new
;;                     (conj fltrd potion)
;;                     fltrd))
;;                 keepers))) 
;;           [] 
;;           combinations))

;; This is slower than the atom version, even for super large seqs
;; (defn create-reducer
;;   [ingredients scoring-fn threshold]
;;   (fn 
;;     ([] [])
;;     ([potions combo]
;;      (let [potion (create-potion ingredients scoring-fn combo)
;;            [_effects {score :score}] potion
;;            [keepers keep-new] (filter-potions potions potion)]
;;        (if (and keep-new (> score threshold))
;;          (conj keepers potion)
;;          potions)))))

;; (defn create-potions
;;   [ingredients scoring-fn threshold combinations]
;;   (r/fold concat (create-reducer ingredients scoring-fn threshold) combinations))

(defn format-potion
  "The 'potion' data structure is a 2-tuple rather than a map and
   the ingredients are lists of integers for performance reasons.
   This function creates a more legible and json-friendly map of 
   the data associated with the potion made from that set of ingredients."
  [ingreds [[negative-effects positive-effects] {:keys [score ingredients]}]]
  {:negative-effects (vec (sort negative-effects))
   :positive-effects (vec (sort positive-effects))
   :score score
   :ingredients (vec (sort (map (comp second first (partial get ingreds)) ingredients)))})

(defn best-by-effect
  [potions]
  (vec (set (filter seq 
                    (vals (reduce (fn [best potion]
                                    (let [{score "score" pos "positive-effects"} potion]
                                      (reduce (fn [res effect]
                                                (let [existing (res effect)]
                                                  (if (or (nil? existing)
                                                          (> score (existing "score")))
                                                    (assoc res effect potion)
                                                    res)))
                                              best
                                              pos)))
                                  (into {} (map (fn [k] [k nil]) positives))
                                  potions))))))
