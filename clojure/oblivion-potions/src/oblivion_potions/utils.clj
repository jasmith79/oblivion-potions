(ns oblivion-potions.utils)

;; Copied from rosetta code
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

(defn partition-with
  [pred coll]
  (reduce (fn [[passes fails] item]
            (if (pred item)
              [(conj passes item) fails]
              [passes (conj fails item)]))
          [[] []]
          coll))

(defn map-with-index-value
  [xs]
  (into {} (map-indexed (fn [idx itm] [itm (inc idx)]) xs)))
