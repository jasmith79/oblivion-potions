(ns oblivion-potions.core
  (:gen-class))

;;;; Imports ;;;;

(use '[clojure.string :as str :only [join split]])
(use '[clojure.set :as nsset :only [subset?]])
;(use '[clojure.math.combinatorics :as combo])
;(use '[clojure.math.numeric-tower :as math :only [expt]])

;;;; Constants ;;;;
(defn now [] (.getTime (new java.util.Date)))
(def start (now))

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

(def MIN_POTION_SCORE 800)
(def MIN_COMBO_SCORE 3000)

(println "Reading file...")

(def ingredients
  (vec (filter
    (fn [line]
      ;; clojure is smart enough here to discard the empty lines, so no need to check the length
      ;; like javascript, however needs the unicode handling for some reason.
      (not (re-find #"(?u),[ \t\xA0\u1680\u180e\u2000-\u200a\u202f\u205f\u3000]*," line)))
    (str/split (slurp "../../allingredients.csv") #"\n"))))

(println "done in " (quot (- (now) start) 1000) " seconds.")

;;;; Functions ;;;;

;;; Returns first match for negative effects, else nil
(defn negative? [effect] (re-find #"(?i)damage|drain|paralyze|burden|silence" effect))

;;; Scores a potion or combo based on the effects
(defn score
  [effs]
  (reduce
    (fn [acc eff]
      (let [i (.indexOf priority eff)]
        (if (= i -1) acc (+ acc (* i i)))))
    0
    effs))

;;; Copied from rosetta code
(defn combinations
  "If m=1, generate a nested list of numbers [0,n)
   If m>1, for each x in [0,n), and for each list in the recursion on [x+1,n), cons the two"
  [m n]
  (letfn [(comb-aux
	   [m start]
	   (if (= 1 m)
	     (for [x (range start n)]
	       (list x))
	     (for [x (range start n)
		   xs (comb-aux (dec m) (inc x))]
	       (cons x xs))))]
    (comb-aux m 0)))

; (defn make-potion
;   "creates a potion data structure"
;   [name & ingreds]
;   (first ingreds))
(defrecord Potion [name effects recipe positives negatives score])
(defn make-potion
  "creates a potion data structure"
  [name & ingreds]
    (let [
      [recipe effects] (reduce
        (fn [accum ingred]
          (let [[ingred_name & ingred_effects] (.split ingred ",")
                [acc_names acc_effects] accum]
            (vector (str acc_names ingred_name) (conj acc_effects ingred_effects))))
        ["" #{}]
        ingreds)
      [negatives positives] ((juxt filter remove) negative? effects)]

      (->Potion name effects recipe positives negatives (score effects))))

(println "Defining potions...")
(def potions
  (filter
    (fn [p]
      (> (:score p) MIN_POTION_SCORE))
    (map
      (fn [combo]
        (apply make-potion (apply str combo) (map (fn [n] (get ingredients n)) combo)))
      (combinations 4 (count ingredients)))))
; (def potions
;   (map
;     (fn [combo]
;       (apply make-potion (apply str combo) (map (fn [n] (get ingredients n)) combo)))
;     (combinations 4 (count ingredients))))

(println "done in " (quot (- (now) start) 1000) " seconds")

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println (count potions))
  (println "Program ran in " (quot (- (now) start) 1000) " seconds")
  (first potions))
