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

var _slicedToArray = (function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; })();

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) arr2[i] = arr[i]; return arr2; } else { return Array.from(arr); } }

function _toArray(arr) { return Array.isArray(arr) ? arr : Array.from(arr); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var priority = ["Reflect Damage", "Reflect Spell", "Fortify Health", "Fortify Endurance", "Fortify Magicka", "Fortify Strength", "Fortify Speed", "Restore Health", "Restore Magicka", "Shock Shield", "Shield", "Fortify Agility", "Fortify Intelligence", "Fortify Willpower", "Fortify Fatigue", "Resist Magicka", "Spell Absorption", "Fire Shield", "Frost Shield", "Restore Endurance", "Light", "Restore Fatigue", "Invisibility", "Fortify Luck", "Chameleon", "Fortify Personality"].reverse();

/*   Imports   */
var fs = require('fs');
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

var scorePotion = function scorePotion(effs) {
  var score = 0;
  effs.forEach(function (eff) {
    var index = priority.indexOf(eff);
    if (index) {
      score += index * index;
    }
  });
  return score;
};

var Potion = function Potion(ingreds) {
  _classCallCheck(this, Potion);

  var _ingreds$reduce = ingreds.reduce(function (acc, ingred) {
    var _ingred$split = ingred.split(',');

    var _ingred$split2 = _toArray(_ingred$split);

    var name = _ingred$split2[0];

    var eff = _ingred$split2.slice(1);

    var _acc = _slicedToArray(acc, 2);

    var names = _acc[0];
    var effs = _acc[1];

    names.push(name);
    effs.push.apply(effs, _toConsumableArray(eff));
    return [names, effs];
  }, [[], []]);

  var _ingreds$reduce2 = _slicedToArray(_ingreds$reduce, 2);

  var recipe = _ingreds$reduce2[0];
  var effects = _ingreds$reduce2[1];

  this.recipe = recipe.join(',');
  this.effects = effects.filter(function (eff, i) {
    return effects.indexOf(eff) !== i;
  });
  this.score = scorePotion(this.effects);
};

var csvList = fs.readFileSync('../allingredients.csv', 'utf-8').split('\n').filter(function (row) {
  return row.length > 3 && !row.match(/,\s*,/g);
}); //filter out one-effect ingreds, empties

var potions = (function (list) {
  var l = list.length,
      arr = [],
      potion = null,
      n = 0;
  for (n; n < l - 3; ++n) {
    var i = 1;
    for (i; i < l - 2; ++i) {
      var j = 2;
      for (j; j < l - 1; ++j) {
        var k = 3;
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
console.log(combinations[combinations.length - 1]);
