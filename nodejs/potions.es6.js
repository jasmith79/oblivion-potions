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
const ARGS      = process.argv.slice(2).join(' ');
const VERBOSE   = ARGS.match(/-[a-z]*v/gi);
const TIMED     = ARGS.match(/-[a-z]*t/gi);
const MIN_SCORE = 800;
const BILLION   = 1e9;
const START     = process.hrtime();
const priority  = [
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

const toSeconds = ([secs, ns]) => ((secs * BILLION + ns) / BILLION).toFixed(2);
const toObject = (list) => list.reduce((acc, k) => {acc[k] = 0; return acc}, {});
const takeTop = (effects) => (item) => {
  let passed = false;
  item.effects.forEach((eff) => {
    if (effects[eff] < 20) {
      passed = true;
    }
    effects[eff] += 1;
  });
  return passed;
};

const scorePotion = (effs) => {
  let score = 0;
  effs.forEach(eff => {
    let index = priority.indexOf(eff);
    if (index !== -1) {
      score += (index * index);
    }
  });
  return score;
}

class Potion {
  constructor (name, ...ingreds) {
    let [recipe, effects] = ingreds.reduce((acc, ingred) => {
      let [name, ...eff] = ingred.split(',');
      let [names, effs]  = acc;
      names.push(name);
      effs.push(...eff);
      return [names, effs];
    }, [[],[]]);
    this.name = '' + name;
    this.recipe = recipe.join(',');
    this.effects = effects.reduce((acc, eff, i) => {
      if (effects.indexOf(eff) !== i && (acc.indexOf(eff) === -1)) {
        acc.push(eff);
      }
      return acc;
    }, []);
    this.score   = scorePotion(this.effects);
  }

  toString () {
    return `${this.name},${this.score},${this.effects.join(',')},${this.recipe}`;
  }
}

let last = START, time = '', timeStr = '';

if (VERBOSE) {
  console.log("Reading file...");
}

let csvList = fs.readFileSync('../allingredients.csv', 'utf-8')
  .split('\n')
  .filter(row => row.length > 3 && !row.match(/,\s*,/g)); //filter out one-effect ingreds, empties

if (VERBOSE) {
  if (TIMED) {
    time = process.hrtime(last);
    last = process.hrtime();
    timeStr = ` in ${toSeconds(time)} seconds`;
  } else {
    timeStr = '';
  }
  console.log(`done${timeStr}. Creating potions...`);
}

let potions = ((list) => {
  let l = list.length, arr = [], potion = null, n = 0;
  for (n; n < l - 3; ++n) {
    let i = n + 1;
    for (i; i < l - 2; ++i) {
      let j = i + 1;
      for (j; j < l - 1; ++j) {
        let k = j + 1
        for (k; k < l; ++k) {
          potion = new Potion(n + i + j + k, list[n], list[i], list[j], list[k]);
          if (potion.score > MIN_SCORE) {
            if (VERBOSE) {
              console.log(potion.recipe);
            }
            arr.push(potion);
          }
        }
      }
    }
  }
  return arr
    .sort((a, b) => b.score - a.score)
    .filter(takeTop(toObject(priority)));
})(csvList);

if (VERBOSE) {
  if (TIMED) {
    time = process.hrtime(last);
    last = process.hrtime();
    timeStr = ` in ${toSeconds(time)} seconds`;
  } else {
    timeStr = '';
  }
  console.log(`done${timeStr}. ${potions.length} potions created. Writing file...`);
}

fs.writeFileSync('./potions.csv', potions.map((p) => p.toString()).join('\n'));

if (VERBOSE) {
  if (TIMED) {
    time = process.hrtime(last);
    last = process.hrtime();
    timeStr = ` in ${toSeconds(time)} seconds`;
  } else {
    timeStr = '';
  }
  console.log(`done${timeStr}. Generating combinations...`);
}

if (TIMED) {
  console.log(`Completed in ${toSeconds(process.hrtime(START))} seconds.`);
}
process.exit(0);
