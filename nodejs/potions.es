#! usr/bin/env node
/*
 *Node.js implementation of my Oblivion Potion Generator.
 *Tested against Node 5.6, can use babel for older versions if desired.
 *
 *
 */
 'use strict';

// /*   Constants   */
const ARGS             = process.argv.slice(2).join(' ');
const VERBOSE          = ARGS.match(/-[a-z]*v/gi);
const TIMED            = ARGS.match(/-[a-z]*t/gi);
const MIN_POTION_SCORE = 800;
const MIN_COMBO_SCORE  = 3000;
const BILLION          = 1e9;
const NEGATIVE         = /burden|paralyze|silence|damage|drain/ig;
const START            = process.hrtime();
const priority         = [
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
const r     = require('./node_modules/ramda/dist/ramda.js');

/*   Functions   */
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

const filterNegatives = (item) => item.match(NEGATIVE);

const strictSuperior = (item, i, arr) => {
  let passed = true;
  for (let j = i + 1; j < arr.length; ++j) {
    if (r.equals(item.positives, arr[j].positives) &&
         (item.negatives.length >= arr[j].negatives.length)) {
      passed = false;
      break;
    }
  }
  return passed;
}

const scoreItem = (effs) => {
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
      acc[0].push(name);
      acc[1].push(...eff);
      return acc;
    }, [[],[]]);
    this.name    = '' + name;
    this.recipe  = recipe;
    this.effects = effects.reduce((acc, eff, i) => {
      if (effects.indexOf(eff) !== i && (acc.indexOf(eff) === -1)) {
        acc.push(eff);
      }
      return acc;
    }, []).sort();
    [this.negatives, this.positives] = r.partition(filterNegatives, this.effects);
    this.score = scoreItem(this.positives);
  }

  toString () {
    return `${this.name},${this.score},${this.effects.join(',')},${this.recipe.join(',')}`;
  }
}

class Combo {
  constructor (...potions) {
    let [recipe, effects] = potions.reduce((acc, potion) => {
      acc[0].push(potion.recipe);
      potions.forEach((potion) => {
        potion.effects.forEach((eff) => {
          if (acc[1].indexOf(eff) === -1) {
            acc[1].push(eff)
          }
        });
      });
      return acc;
    }, [[], []]);
    this.recipe = recipe.join(',');
    this.effects = effects.sort();
    [this.negatives, this.positives] = r.partition(filterNegatives, this.effects);
    this.score = scoreItem(this.positives);
  }

  toString () {
    return `${this.recipe},${this.score},${this.effects.join(',')}`;
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

//because of the dataset size, for loops and copy/paste are vastly more efficient here than mapping
//over ranges and encapsulating it in a function or trampolining the general-case recursive solution
//it literally shaves *minutes* off the runtime of the program.
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
          if (potion.score > MIN_POTION_SCORE) {
            // if (VERBOSE) {
            //   console.log(potion.recipe);
            // }
            arr.push(potion);
          }
        }
      }
    }
  }
  return arr
    .sort((a, b) => b.score - a.score)
    .filter(takeTop(toObject(priority)))
    .filter(strictSuperior);
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

let combos = ((list) => {
  let l = list.length, arr = [], combo = null, n = 0;
  for (n; n < l - 3; ++n) {
    let i = n + 1;
    for (i; i < l - 2; ++i) {
      let j = i + 1;
      for (j; j < l - 1; ++j) {
        let k = j + 1
        for (k; k < l; ++k) {
          combo = new Combo(list[n], list[i], list[j], list[k]);
          if (combo.score > MIN_COMBO_SCORE) {
            // if (VERBOSE) {
            //   console.log(combo.recipe);
            // }
            arr.push(combo);
          }
        }
      }
    }
  }
  return arr
    .sort((a, b) => b.score - a.score)
    .filter(takeTop(toObject(priority)))
    .filter(strictSuperior);
})(potions);

if (VERBOSE) {
  if (TIMED) {
    time = process.hrtime(last);
    last = process.hrtime();
    timeStr = ` in ${toSeconds(time)} seconds`;
  } else {
    timeStr = '';
  }
  console.log(`done${timeStr}. Writing File...`);
}

let shoppingList = [];
fs.writeFileSync(
  './combos.csv',
  combos
    .map((c) => {
      shoppingList.push(c.recipe);
      c.toString()
    })
    .join('\n')
);

if (VERBOSE) {
  if (TIMED) {
    time = process.hrtime(last);
    last = process.hrtime();
    timeStr = ` in ${toSeconds(time)} seconds`;
  } else {
    timeStr = '';
  }
  console.log(`done${timeStr}. Writing Shopping List...`);
}

fs.writeFileSync('./shopping.csv', shoppingList.join('\n'));

if (TIMED) {
  console.log(`Completed in ${toSeconds(process.hrtime(START))} seconds.`);
}
process.exit(0);
