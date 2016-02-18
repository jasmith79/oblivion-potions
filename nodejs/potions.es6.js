#! usr/bin/env node
/*
 *Node.js implementation of my Oblivion Potion Generator.
 *Tested against Node 5.6, can use babel for older versions if desired.
 *
 *
 */
 'use strict';

// /*   Constants   */
// const MAX_INGREDIENTS       = 4;
// const MAX_INGREDIENTS_ARRAY = [0, 1, 2, 3];
// const notAFunctionError     = new Error('Missing function argument');
const priority = [
    "Reflect Damage",
    "Reflect Spell",
    "Fortify Health",
    "Fortify Endurance",
    "Fortify Magicka",
    "Fortify Strength",
    "Fortify Speed",
    "Restore Health",
    "Restore Magicka",
    "Shock Shield",
    "Shield",
    "Fortify Agility",
    "Fortify Intelligence",
    "Fortify Willpower",
    "Fortify Fatigue",
    "Resist Magicka",
    "Spell Absorption",
    "Fire Shield",
    "Frost Shield",
    "Restore Endurance",
    "Light",
    "Restore Fatigue",
    "Invisibility",
    "Fortify Luck",
    "Chameleon",
    "Fortify Personality"
].reverse();

/*   Imports   */
const fs    = require('fs');
// const r     = require('../bower_components/ramda/dist/ramda.js');
// const d     = require('../bower_components/decorators-js/decorators.js');
// const math  = require('../bower_components/mathjs/dist/math.js');

/*   Functions   */

// const zipN = (...args) => {
//   let fn = typeof args[args.length - 1] === 'function' ? args.pop() : (...fnArgs) => fnArgs;
//   let arr = [];
//   let shortest = args.reduce((acc, x) => Math.min(acc, x.length), Number.MAX_SAFE_INTEGER);
//   for (let i = 0; i < shortest; ++i) {
//     arr.push(fn(...args.map(x => x[i])));
//   }
//   return arr;
// };

const scorePotion = (effs) => {
  let score = 0;
  effs.forEach(eff => {
    let index = priority.indexOf(eff);
    if (index) {
      score += (index * index);
    }
  });
  return score;
}

class Potion {
  constructor (ingreds) {

    let [recipe, effects] = ingreds.reduce((acc, ingred) => {
      let [name, ...eff] = ingred.split(',');
      let [names, effs]  = acc;
      names.push(name);
      effs.push(...eff);
      return [names, effs];
    }, [[],[]])

    this.recipe = recipe.join(',');
    this.effects = effects.filter((eff, i) => effects.indexOf(eff) !== i);
    this.score   = scorePotion(this.effects);
  }
}


let csvList = fs.readFileSync('../allingredients.csv', 'utf-8')
  .split('\n')
  .filter(row => row.length > 3 && !row.match(/,\s*,/g)); //filter out one-effect ingreds, empties

let potions = ((list) => {
  let l = list.length, arr = [], potion = null, n = 0;
  for (n; n < l - 3; ++n) {
    let i = 1;
    for (i; i < l - 2; ++i) {
      let j = 2;
      for (j; j < l - 1; ++j) {
        let k = 3
        for (k; k < l; ++k) {
          potion = new Potion([list[n], list[i], list[j], list[k]]);
          if (potion.score > 500) {
            console.log(potion.recipe);
            arr.push(potion);
          }
        }
      }
    }
  }
  return arr;
})(csvList);

console.log(combinations.length);
console.log(combinations[0]);
console.log(combinations[combinations.length - 1])
