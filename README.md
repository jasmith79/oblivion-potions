#Oblivion Potion Generator

##The Why

I truly enjoy playing Elder Scrolls games, and Oblivion is no exception. But the Alchemy skill is seriously
nerfed compared to previous TES titles. Since you can only drink four potions at once\*, it pays to ensure
that optimal benefits are conferred. This seemed like a reasonably simple problem conceptually: create a
de-duped list of the most beneficial potions that can be created out of ingredients that can be acquired
in potentially unlimited quantities, along with their effects and recipes.

This problem is actually surprisingly tricky to solve performantly (at least for me). There are
approximately 100 ingredients in Oblivion, and potions can be made by mixing up to four of them.
By combination there are about 4 million possibilities that have to be tested for dupes, assessed/scored,
sorted, and printed.

Fortunately there is plenty of potential for parallelization of the process. One of my ultimate goals of
this project is using it for language acquisition.
