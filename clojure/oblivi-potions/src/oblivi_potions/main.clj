(ns oblivi-potions.core
	(:gen-class))

;;;;imports;;;;

(use '[clojure.string :as str :only [join split]])
(use '[clojure.set :as nsset :only [subset?]])
(use '[clojure.math.combinatorics :as combo])
(use '[clojure.math.numeric-tower :as math :only [expt]])

;;;;decorators;;;;

; (defn memoize [f]
;   (let [mem (atom {})]
;     (fn [& args]
;       (if-let [e (find @mem args)]
;         (val e)
;         (let [ret (apply f args)]
;           (swap! mem assoc args ret)
;           ret)))))

;;like memoize but in the repeated case returns false instead of caching the value of applying f to
;;args, meant to decorate a filtering function to also filter out repeated filtering of similar args
(defn once-per [f]
  (let [already (atom #{})]
    (fn [& args]
      (if-let [done (@already args)]
        (val false)
        (let [ret (apply f args)]
          (swap! already conj args)
          ret)))))

(def priority #{
    "Reflect Damage"
    "Reflect Spell"
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
    "Fortify Fatigue"
    "Resist Magicka"
    "Spell Absorption"
    "Fire Shield"
    "Frost Shield"
    "Restore Endurance"
    "Light"
    "Restore Fatigue"
    "Invisibility"
    "Fortify Luck"
    "Chameleon"
    "Fortify Personality"})

;;Returns first match for negative effects, else nil
(defn negative? [effect] (re-find #"(?i)damage|drain|paralyze|burden|silence" effect))
(defn positive? [effect] (not (negative? effect)))

;;Returns a filtering fn accumulating least n items per each effect in the collection
;;NOTE ***Presumes the collection being reduced is sorted descending by score***
(defn get-top [n, ref-coll]
  (let [desired (atom (reduce (fn [accum, key] (assoc accum key 0)) {} ref-coll))]
    (once-per
      (fn [effects]
        (let [d @desired
              passed (some
                (fn [effect]
                  (and (contains? d effect)
                  (< ((swap! desired update effect inc) effect) n))))]
          passed)))))

(defn score [effects]
  (reduce + (map (fn [effect]
                   (let [index (.indexOf priority effect)]
                     (if (= -1 index)
                         (val 0)
                         (+ (expt index 2))))) (set effects))))
                       ; (+ (expt index 2)
                       ;    (count (filter (fn [eff] (= eff effect)) effects)))))) (set effects)))))


(def score-item (memoize score))
(def filter-ingred
  (once-per
    (fn [effects]
      (and (every? (fn [effect] (> (count effect) 1) effects))
           (some priority effects)))))

(def ingred-list
  (filter (fn [ingred] (filter-ingred (rest ingred))) (map (fn [ln] (str/split ln #","))
       (str/split (slurp "resources/csv/allingredients.csv") #"\n"))))
        ;   (sort-by
        ;     (fn [ingred] (score-item (rest ingred)))
        ;     >
        ;     (map (fn [ln] (str/split ln #","))
        ;          (str/split (slurp "resources/csv/allingredients.csv") #"\n")))))

(defn -main
  "Hi! I'm a docstring!"
  [& args]
  (println (count ingred-list)))
