; (ns oblivi-potions.core
; 	(:gen-class))
;
; ;;imports;;
;
; (use '[clojure.string :as str :only [join split]])
; (use '[clojure.set :as nsset :only [subset?]])
; (use '[clojure.math.combinatorics :as combo])
; (use '[clojure.math.numeric-tower :as math :only [expt]])
;
; ;;utils;;
;
; (defn memoize [f]
;   (let [mem (atom {})]
;     (fn [& args]
;       (if-let [e (find @mem args)]
;         (val e)
;         (let [ret (apply f args)]
;           (swap! mem assoc args ret)
;           ret)))))
;
; (def priority [
;     "Reflect Damage"
;     "Reflect Spell"
;     "Fortify Health"
;     "Fortify Endurance"
;     "Fortify Magicka"
;     "Fortify Strength"
;     "Fortify Speed"
;     "Restore Health"
;     "Restore Magicka"
;     "Shock Shield"
;     "Shield"
;     "Fortify Agility"
;     "Fortify Intelligence"
;     "Fortify Willpower"
;     "Fortify Fatigue"
;     "Resist Magicka"
;     "Spell Absorption"
;     "Fire Shield"
;     "Frost Shield"
;     "Restore Endurance"
;     "Light"
;     "Restore Fatigue"
;     "Invisibility"
;     "Fortify Luck"
;     "Chameleon"
;     "Fortify Personality"])
;
; ;;Returns first match for negative effects, else nil
; (defn negative? [effect] (re-find #"(?i)damage|drain|paralyze|burden|silence" effect))
; (defn positive? [effect] (not (negative? effect)))
;
; ;;Returns a filtering fn accumulating least n items per each effect in the collection
; ;;NOTE ***Presumes the collection being reduced is sorted descending by score***
; (defn getTop [n, coll]
;   (let [desired (reduce (fn [accum, key] (assoc accum key 0)) {} coll)]
; 	(fn [item]
; 	  (if (some (fn [effect]
;   				  (and (contains? desired effect)
;   					   (< (get (update desired effect inc) effect) n)))
;   				(get item :effects)) true false))))
;
; ; (defn score-item [effects]
; ;   (let [numTimes (reduce (fn [accum, effect]
; ; 	(if (contains? accum effect)
; ; 		(update accum effect inc)
; ; 		(assoc accum effect 0))) {} effects)]
; ;     (reduce + (map (fn [[effect, times]] (let [index (.indexOf priority effect)]
; ;       (if (= index -1) 0 (+ (expt index 2) times)))) numTimes))))
;
; (defn score-item)
;
; ;;helper fn for strict-superior, returns false if a's positive effects are a subset of b's and
; ;;b's score is >= else true
; (defn compare-superior [a, b]
;   (if (= a b) ;need this otherwise everything fails test when compared to itself
;       true
;       (if (and (nsset/subset? (set (filter positive? (a :effects)))
; 	                          (set (filter positive? (b :effects))))
;                (>= (b :score) (a :score))) false true)))
;
; ;;Returns a filtering function. Checks for a better potion with the same positive effects.
; (defn strict-superior [comparison]
;   (fn [item] (every? (fn [other-item] (compare-superior item other-item)) comparison)))
;
; (defn make-potion [ingredients]
;   (let [effs (flatten (map rest ingredients))]
;     {:recipe (map first ingredients)
;      :all-effects effs,
;      :effects (set effs),
;      :score (score-item effs),
;      :name (gensym "#")}))
;
; ;;;want to filter out one effect ingredients and ones with only negative effects. Apparently the
; ;;;blank space is not really blank, precluding use of clojure.string.blank? hence the length check
; ;(not (every? (fn [effect] (negative? effect)) effects))))
; (def ingred-list (filter
;   (fn [effects]
;     (and (every? (fn [effect] (> (count effect) 1)) effects)
; 		 (some (fn [effect] (some #{effect} priority)) effects)))
;   (map (fn [ln] (str/split ln #","))
; 	   (str/split (slurp "resources/csv/allingredients.csv") #"\n"))))
;
; (println "done with ingred-list " (count ingred-list))
; (doall (map println (map (fn [arr] (join "," arr)) ingred-list)))
;
; ;note to self, need lambda here because default sort is ascending
; (def potions (sort-by :score
; 	                  (fn [a, b] (compare b a))
;                       (map make-potion (combo/combinations ingred-list 4))))
;
; (println "done with making/sorting potions")
;
; ;;;need double call to filter here because outer compares every element against every other, need
; ;;;to work with subset to make runtime acceptable
; (def filtered-potions (filter strict-superior (filter (getTop 20 priority) potions)))
; ;(def potion-combos combo/combinations () 4)
;
; (defn -main
;   "Hi! I'm a docstring!"
;   [& args]
;   (println (first filtered-potions)))
;   ;(doall (map println (map (fn [arr] (join "," arr)) ingred-list))))

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
; (defn no-subsets [f]
;   (let [already (atom #{})]
;     (fn [& args]
;       (if-let [done (@already args)]
;         (val false)
;         (let [ret (apply f args)]
;           (swap! already conj args)
;           ret)))))

(defn no-subsets [f]
  (let [alreadys #{}]
    (fn [args]
      (if-let [already (some (fn [rec] (nsset/subset? args rec)) alreadys)]
        (do
		  ;(println already)
		  ;(println args)
		  (println "oops")
		  false)
        (let [result (f args)]
          (conj alreadys args)
          result)))))

(def priority [
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
    "Fortify Personality"])

;;Returns first match for negative effects, else nil
(defn negative? [effect] (re-find #"(?i)damage|drain|paralyze|burden|silence" effect))
(defn positive? [effect] (not (negative? effect)))

;;Returns a filtering fn accumulating least n items per each effect in the collection
;;NOTE ***Presumes the collection being reduced is sorted descending by score***
(defn get-top [n, ref-coll]
  (let [desired (atom (reduce (fn [accum, key] (assoc accum key 0)) {} ref-coll))]
    (no-subsets
      (fn [effects]
	    (some
		  (fn [effect]
			(if-let [counter (@desired effect)]
			  (do
				(swap! desired assoc effect (inc counter))
				(< counter n))
              false))
		  effects)))))

(defn score [effects]
  (reduce
	+
	(map (fn [effect]
      (let [index (.indexOf priority effect)]
        (if (= -1 index) 0 (+ (expt (- (count priority) index) 2)))))
	(set effects))))

(def score-item (memoize score))
; (defn filter-ingred [effects]
;   (and (every? (fn [effect] (> (count effect) 1)) effects)
; 	   (some (set priority) effects)))
; (def filter-ingred
;   (no-subsets
;     (fn [effects]
;       (and (every? (fn [effect] (> (count effect) 1)) effects)
; 	       (some (set priority) effects)))))

(def filter-ingred (get-top 20 priority))

(def ingred-list
  (filter
    (fn [ingred] (filter-ingred (filter positive? (rest ingred))))
    (sort-by
      (fn [ingred] (score-item (rest ingred)))
      >
      (map (fn [ln] (str/split ln #","))
      (str/split (slurp "resources/csv/allingredients.csv") #"\n")))))

(defn -main [& args]
  (println (count ingred-list))
  (println (first ingred-list))
  (println (score-item (first ingred-list))))
