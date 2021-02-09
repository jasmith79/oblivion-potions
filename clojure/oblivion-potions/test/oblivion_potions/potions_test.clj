(ns oblivion-potions.potions-test
  (:require
   [clojure.test :refer :all]
   [oblivion-potions.potions :refer :all]
   [oblivion-potions.utils :refer [combinations]]
   [oblivion-potions.scoring :refer [default-scorer]]))

(deftest test-negative?
  (testing "Should be negative"
    (is (negative? "Damage Fatigue"))
    (is (negative? "Damage Magicka"))
    (is (negative? "Damage Luck"))
    (is (negative? "Damage Health"))
    (is (negative? "Damage Speed"))
    (is (negative? "Fire Damage"))
    (is (negative? "Frost Damage"))
    (is (negative? "Shock Damage"))
    (is (negative? "Burden"))
    (is (negative? "Paralysis"))
    (is (negative? "Silence"))
    (is (negative? "Drain Health")))
  (testing "Should not be negative"
    (is (not (negative? "Resist Poison")))
    (is (not (negative? "Fortify Endurance")))
    (is (not (negative? "Night-Eye")))
    (is (not (negative? "Invisibility")))
    (is (not (negative? "Detect Life")))
    (is (not (negative? "Cure Disease")))
    (is (not (negative? "Resist Disease")))))

(deftest test-create-ingredient
  (testing "Happy path"
    (let [ingred (create-ingredient ["Alkanet Flower"
                                     "Restore Intelligence"
                                     "Resist Poison"
                                     "Light"
                                     "Damage Fatigue"])]
      (is (= "Alkanet Flower" (ingred :name)))
      (is (= ["Restore Intelligence"
              "Resist Poison"
              "Light"
              "Damage Fatigue"]
             (ingred :effects))))))

(deftest test-calc-effects
  (testing "Happy path"
    (let [dummy [(create-ingredient ["Arrowroot"
                                     "Restore Agility"
                                     "Damage Luck"
                                     "Fortify Strength"
                                     "Burden"])
                 (create-ingredient ["Blackberry"
                                     "Restore Fatigue"
                                     "Resist Shock"
                                     "Fortify Endurance"
                                     "Restore Magicka"])
                 (create-ingredient ["Bloodgrass"
                                     "Chameleon"
                                     "Resist Paralysis"
                                     "Burden"
                                     "Fortify Health"])
                 (create-ingredient ["Void EssenceSI"
                                     "Restore Health"
                                     "Fortify Health"
                                     "Fortify Strength"
                                     "Fortify Endurance"])]
          [negatives positives] (calc-effects dummy)]
      (is (= #{"Burden"} negatives))
      (is (= #{"Fortify Health" "Fortify Strength" "Fortify Endurance"} positives)))))

(deftest test-create-ingredients
  (testing "Happy path"
    (let [dummy [["Blackberry"
                  "Restore Fatigue"
                  "Resist Shock"
                  "Fortify Endurance"
                  "Restore Magicka"]
                 ["Arrowroot"
                  "Restore Agility"
                  "Damage Luck"
                  "Fortify Strength"
                  "Burden"]]
          result {0 {:name "Blackberry"
                     :effects '("Restore Fatigue"
                                "Resist Shock"
                                "Fortify Endurance"
                                "Restore Magicka")}
                  1 {:name "Arrowroot"
                     :effects '("Restore Agility"
                                "Damage Luck"
                                "Fortify Strength"
                                "Burden")}}]
      (is (= result (create-ingredients dummy))))))

(deftest test-better?
  (let [potion-a [#{"Burden"}
                  #{"Fortify Health" "Fortify Strength" "Fortify Endurance"}]
        potion-b [#{"Burden"}
                  #{"Fortify Health" "Fortify Strength" "Fortify Endurance" "Restore Magicka"}]
        potion-c [#{"Burden" "Paralysis"}
                  #{"Fortify Health" "Fortify Strength" "Fortify Endurance"}]
        potion-d [#{"Burden" "Paralysis"}
                  #{"Fortify Health" "Fortify Strength" "Fortify Endurance" "Restore Magicka"}]]
    (testing "Should be better"
      (is (better? potion-b potion-a))
      (is (better? potion-a potion-c)))
    (testing "Should not be better"
      (is (not (better? potion-a potion-a)))
      (is (not (better? potion-a potion-b)))
      (is (not (better? potion-c potion-a))))
    (testing "False positives?"
      (is (not (better? potion-d potion-a))))))

(deftest test-filter-potions
  (testing "Old and new are same"
    (let [ps [[[#{} #{"Restore Health" "Fortify Health" "Fortify Strength" "Fortify Endurance"}] :a]
              [[#{} #{"Restore Health" "Fortify Health" "Fortify Strength" "Fortify Endurance"}] :b]]
          c [[#{} #{"Restore Health" "Fortify Health" "Fortify Strength" "Fortify Endurance"}] :c]
          [fltrd keep-potion] (filter-potions ps c)]
      (is (empty? fltrd))
      (is (true? keep-potion))))
  (testing "Old is worse"
    (let [ps [[[#{} #{"Fortify Health" "Fortify Strength"}] :a]
              [[#{"Damage Fatigue"} #{"Fortify Health" "Fortify Strength"}] :b]]
          c [[#{} #{"Fortify Health" "Fortify Strength"}] :c]
          [fltrd keep-potion] (filter-potions ps c)]
      (is (empty? fltrd))
      (is (true? keep-potion))))
  (testing "New is worse"
     (let [ps [[[#{} #{"Restore Health" "Fortify Health" "Fortify Strength" "Fortify Endurance"}] :a]]
           b [[#{"Damage Health"} #{"Restore Health" "Fortify Health" "Fortify Strength" "Fortify Endurance"}] :b]
           [fltrd keep-potion] (filter-potions ps b)]
       (is (= (first ps) (first fltrd)))
       (is (= 1 (count fltrd)))
       (is (false? keep-potion))))
  (testing "New is different"
    (let [ps [[[#{} #{"Restore Health" "Fortify Health" "Fortify Strength" "Fortify Endurance"}] :a]]
           b [[#{"Damage Health"} #{"Restore Health" "Fortify Health" "Fortify Strength" "Fortify Magicka"}] :b]
          [fltrd keep-potion] (filter-potions ps b)]
      (is (= (first ps) (first fltrd)))
      (is (= 1 (count fltrd)))
      (is (true? keep-potion)))))

(deftest test-create-potion
  (testing "Happy path"
    (let [ingredients [(create-ingredient ["Arrowroot"
                                           "Restore Agility"
                                           "Damage Luck"
                                           "Fortify Strength"
                                           "Burden"])
                       (create-ingredient ["Blackberry"
                                           "Restore Fatigue"
                                           "Resist Shock"
                                           "Fortify Endurance"
                                           "Restore Magicka"])
                       (create-ingredient ["Bloodgrass"
                                           "Chameleon"
                                           "Resist Paralysis"
                                           "Burden"
                                           "Fortify Health"])
                       (create-ingredient ["Void EssenceSI"
                                           "Restore Health"
                                           "Fortify Health"
                                           "Fortify Strength"
                                           "Fortify Endurance"])]
          f (fn [& _args] 100)
          combination '(0 1 2 3)
          result [[#{"Burden"}
                   #{"Fortify Health" "Fortify Strength" "Fortify Endurance"}]
                  {:ingredients '(0 1 2 3)
                   :score 100}]]
      (is (= result (create-potion ingredients f combination))))))

(deftest test-create-potions
  (testing "Happy path"
    (let [ingredients [(create-ingredient ["Arrowroot"
                                           "Restore Agility"
                                           "Damage Luck"
                                           "Fortify Strength"
                                           "Burden"])
                       (create-ingredient ["Blackberry"
                                           "Restore Fatigue"
                                           "Resist Shock"
                                           "Fortify Endurance"
                                           "Restore Magicka"])
                       (create-ingredient ["Bloodgrass"
                                           "Chameleon"
                                           "Resist Paralysis"
                                           "Burden"
                                           "Fortify Health"])
                       (create-ingredient ["Void EssenceSI"
                                           "Restore Health"
                                           "Fortify Health"
                                           "Fortify Strength"
                                           "Fortify Endurance"])
                       (create-ingredient ["Boar Meat"
                                           "Restore Health"
                                           "Damage Speed"
                                           "Fortify Health"
                                           "Burden"])]
          combos (combinations 4 (count ingredients))
          threshold 0
          results [[[#{"Burden"} 
                     #{"Fortify Health" "Fortify Strength" "Restore Health" "Fortify Endurance"}] 
                    {:ingredients '(0 1 3 4), :score 3311}]]]
        (is (= results (create-potions ingredients default-scorer threshold combos))))))

(deftest test-format-potion
  (testing "Happy path"
    (let [ingredients [(create-ingredient ["Arrowroot"
                                           "Restore Agility"
                                           "Damage Luck"
                                           "Fortify Strength"
                                           "Burden"])
                       (create-ingredient ["Blackberry"
                                           "Restore Fatigue"
                                           "Resist Shock"
                                           "Fortify Endurance"
                                           "Restore Magicka"])
                       (create-ingredient ["Bloodgrass"
                                           "Chameleon"
                                           "Resist Paralysis"
                                           "Burden"
                                           "Fortify Health"])
                       (create-ingredient ["Void EssenceSI"
                                           "Restore Health"
                                           "Fortify Health"
                                           "Fortify Strength"
                                           "Fortify Endurance"])]
          f (fn [& _args] 100)
          combination '(0 1 2 3)
          potion (create-potion ingredients f combination)
          result {:negative-effects ["Burden"]
                  :positive-effects ["Fortify Endurance" "Fortify Health" "Fortify Strength"]
                  :score 100
                  :ingredients ["Arrowroot" "Blackberry" "Bloodgrass" "Void EssenceSI"]}]
        (is (= result (format-potion ingredients potion))))))
