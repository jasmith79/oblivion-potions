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

var _slicedToArray = (function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i['return']) _i['return'](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError('Invalid attempt to destructure non-iterable instance'); } }; })();

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ('value' in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) arr2[i] = arr[i]; return arr2; } else { return Array.from(arr); } }

function _toArray(arr) { return Array.isArray(arr) ? arr : Array.from(arr); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError('Cannot call a class as a function'); } }

var ARGS = process.argv.slice(2).join(' ');
var VERBOSE = ARGS.match(/-[a-z]*v/gi);
var TIMED = ARGS.match(/-[a-z]*t/gi);
var MIN_SCORE = 800;
var BILLION = 1e9;
var START = process.hrtime();
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

var toSeconds = function toSeconds(_ref) {
  var _ref2 = _slicedToArray(_ref, 2);

  var secs = _ref2[0];
  var ns = _ref2[1];
  return ((secs * BILLION + ns) / BILLION).toFixed(2);
};
var toObject = function toObject(list) {
  return list.reduce(function (acc, k) {
    acc[k] = 0;
    return acc;
  }, {});
};
var takeTop = function takeTop(effects) {
  return function (item) {
    var passed = false;
    item.effects.forEach(function (eff) {
      if (effects[eff] < 20) {
        passed = true;
      }
      effects[eff] += 1;
    });
    return passed;
  };
};

var scorePotion = function scorePotion(effs) {
  var score = 0;
  effs.forEach(function (eff) {
    var index = priority.indexOf(eff);
    if (index !== -1) {
      score += index * index;
    }
  });
  return score;
};

var Potion = (function () {
  function Potion(name) {
    _classCallCheck(this, Potion);

    for (var _len = arguments.length, ingreds = Array(_len > 1 ? _len - 1 : 0), _key = 1; _key < _len; _key++) {
      ingreds[_key - 1] = arguments[_key];
    }

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

    this.name = '' + name;
    this.recipe = recipe.join(',');
    this.effects = effects.reduce(function (acc, eff, i) {
      if (effects.indexOf(eff) !== i && acc.indexOf(eff) === -1) {
        acc.push(eff);
      }
      return acc;
    }, []);
    this.score = scorePotion(this.effects);
  }

  _createClass(Potion, [{
    key: 'toString',
    value: function toString() {
      return this.name + ',' + this.score + ',' + this.effects.join(',') + ',' + this.recipe;
    }
  }]);

  return Potion;
})();

var last = START,
    time = '',
    timeStr = '';

if (VERBOSE) {
  console.log("Reading file...");
}

var csvList = fs.readFileSync('../allingredients.csv', 'utf-8').split('\n').filter(function (row) {
  return row.length > 3 && !row.match(/,\s*,/g);
}); //filter out one-effect ingreds, empties

if (VERBOSE) {
  if (TIMED) {
    time = process.hrtime(last);
    last = process.hrtime();
    timeStr = ' in ' + toSeconds(time) + ' seconds';
  } else {
    timeStr = '';
  }
  console.log('done' + timeStr + '. Creating potions...');
}

var potions = (function (list) {
  var l = list.length,
      arr = [],
      potion = null,
      n = 0;
  for (n; n < l - 3; ++n) {
    var i = n + 1;
    for (i; i < l - 2; ++i) {
      var j = i + 1;
      for (j; j < l - 1; ++j) {
        var k = j + 1;
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
  return arr.sort(function (a, b) {
    return b.score - a.score;
  }).filter(takeTop(toObject(priority)));
})(csvList);

if (VERBOSE) {
  if (TIMED) {
    time = process.hrtime(last);
    last = process.hrtime();
    timeStr = ' in ' + toSeconds(time) + ' seconds';
  } else {
    timeStr = '';
  }
  console.log('done' + timeStr + '. ' + potions.length + ' potions created. Writing file...');
}

fs.writeFileSync('./potions.csv', potions.map(function (p) {
  return p.toString();
}).join('\n'));

if (VERBOSE) {
  if (TIMED) {
    time = process.hrtime(last);
    last = process.hrtime();
    timeStr = ' in ' + toSeconds(time) + ' seconds';
  } else {
    timeStr = '';
  }
  console.log('done' + timeStr + '. Generating combinations...');
}

if (TIMED) {
  console.log('Completed in ' + toSeconds(process.hrtime(START)) + ' seconds.');
}
process.exit(0);
