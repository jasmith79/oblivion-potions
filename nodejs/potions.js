#! usr/bin/env node

/*
 *Node.js implementation of my Oblivion Potion Generator.
 *Tested against Node 5.6, can use babel for older versions if desired.
 *
 *
 */
'use strict';

// /*   Constants   */

var _slicedToArray = (function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i['return']) _i['return'](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError('Invalid attempt to destructure non-iterable instance'); } }; })();

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ('value' in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) arr2[i] = arr[i]; return arr2; } else { return Array.from(arr); } }

function _toArray(arr) { return Array.isArray(arr) ? arr : Array.from(arr); }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError('Cannot call a class as a function'); } }

var ARGS = process.argv.slice(2).join(' ');
var VERBOSE = ARGS.match(/-[a-z]*v/gi);
var TIMED = ARGS.match(/-[a-z]*t/gi);
var MIN_POTION_SCORE = 800;
var MIN_COMBO_SCORE = 3000;
var BILLION = 1e9;
var NEGATIVE = /burden|paralyze|silence|damage|drain/ig;
var START = process.hrtime();
var priority = ["Reflect Damage", "Reflect Spell", "Fortify Health", "Fortify Endurance", "Fortify Magicka", "Fortify Strength", "Fortify Speed", "Restore Health", "Restore Magicka", "Shock Shield", "Shield", "Fortify Agility", "Fortify Intelligence", "Fortify Willpower", "Fortify Fatigue", "Resist Magicka", "Spell Absorption", "Fire Shield", "Frost Shield", "Restore Endurance", "Light", "Restore Fatigue", "Invisibility", "Fortify Luck", "Chameleon", "Fortify Personality"].reverse();

/*   Imports   */
var fs = require('fs');
var r  = require('../bower_components/ramda/dist/ramda.js');

/*   Functions   */
var toSeconds = function toSeconds(_ref) {
  var _ref2 = _slicedToArray(_ref, 2);

  var secs = _ref2[0];
  var ns = _ref2[1];
  return ((secs * BILLION + ns) / BILLION).toFixed(2);
};
var toObject = function toObject(list) {
  return list.reduce(function (acc, k) {
    acc[k] = 0;return acc;
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

var filterNegatives = function filterNegatives(item) {
  return item.match(NEGATIVE);
};

var strictSuperior = function strictSuperior(item, i, arr) {
  var passed = true;
  for (var j = i + 1; j < arr.length; ++j) {
    if (r.equals(item.positives, arr[j].positives) && item.negatives.length >= arr[j].negatives.length) {
      passed = false;
      break;
    }
  }
  return passed;
};

var scoreItem = function scoreItem(effs) {
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
      var _acc$1;

      var _ingred$split = ingred.split(',');

      var _ingred$split2 = _toArray(_ingred$split);

      var name = _ingred$split2[0];

      var eff = _ingred$split2.slice(1);

      var _acc = _slicedToArray(acc, 2);

      var names = _acc[0];
      var effs = _acc[1];

      acc[0].push(name);
      (_acc$1 = acc[1]).push.apply(_acc$1, _toConsumableArray(eff));
      return acc;
    }, [[], []]);

    var _ingreds$reduce2 = _slicedToArray(_ingreds$reduce, 2);

    var recipe = _ingreds$reduce2[0];
    var effects = _ingreds$reduce2[1];

    this.name = '' + name;
    this.recipe = recipe;
    this.effects = effects.reduce(function (acc, eff, i) {
      if (effects.indexOf(eff) !== i && acc.indexOf(eff) === -1) {
        acc.push(eff);
      }
      return acc;
    }, []).sort();

    var _r$partition = r.partition(filterNegatives, this.effects);

    var _r$partition2 = _slicedToArray(_r$partition, 2);

    this.negatives = _r$partition2[0];
    this.positives = _r$partition2[1];

    this.score = scoreItem(this.positives);
  }

  _createClass(Potion, [{
    key: 'toString',
    value: function toString() {
      return this.name + ',' + this.score + ',' + this.effects.join(',') + ',' + this.recipe.join(',');
    }
  }]);

  return Potion;
})();

var Combo = (function () {
  function Combo() {
    for (var _len2 = arguments.length, potions = Array(_len2), _key2 = 0; _key2 < _len2; _key2++) {
      potions[_key2] = arguments[_key2];
    }

    _classCallCheck(this, Combo);

    var _potions$reduce = potions.reduce(function (acc, potion) {
      acc[0].push(potion.recipe);
      potions.forEach(function (potion) {
        potion.effects.forEach(function (eff) {
          if (acc[1].indexOf(eff) === -1) {
            acc[1].push(eff);
          }
        });
      });
      return acc;
    }, [[], []]);

    var _potions$reduce2 = _slicedToArray(_potions$reduce, 2);

    var recipe = _potions$reduce2[0];
    var effects = _potions$reduce2[1];

    this.recipe = recipe.join(',');
    this.effects = effects.sort();

    var _r$partition3 = r.partition(filterNegatives, this.effects);

    var _r$partition32 = _slicedToArray(_r$partition3, 2);

    this.negatives = _r$partition32[0];
    this.positives = _r$partition32[1];

    this.score = scoreItem(this.positives);
  }

  _createClass(Combo, [{
    key: 'toString',
    value: function toString() {
      return this.recipe + ',' + this.score + ',' + this.effects.join(',');
    }
  }]);

  return Combo;
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

//because of the dataset size, for loops and copy/paste are vastly more efficient here than mapping
//over ranges and encapsulating it in a function or trampolining the general-case recursive solution
//it literally shaves *minutes* off the runtime of the program.
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
          if (potion.score > MIN_POTION_SCORE) {
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
  }).filter(takeTop(toObject(priority))).filter(strictSuperior);
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

var combos = (function (list) {
  var l = list.length,
      arr = [],
      combo = null,
      n = 0;
  for (n; n < l - 3; ++n) {
    var i = n + 1;
    for (i; i < l - 2; ++i) {
      var j = i + 1;
      for (j; j < l - 1; ++j) {
        var k = j + 1;
        for (k; k < l; ++k) {
          combo = new Combo(list[n], list[i], list[j], list[k]);
          if (combo.score > MIN_COMBO_SCORE) {
            if (VERBOSE) {
              console.log(combo.recipe);
            }
            arr.push(combo);
          }
        }
      }
    }
  }
  return arr.sort(function (a, b) {
    return b.score - a.score;
  }).filter(takeTop(toObject(priority))).filter(strictSuperior);
})(potions);

if (VERBOSE) {
  if (TIMED) {
    time = process.hrtime(last);
    last = process.hrtime();
    timeStr = ' in ' + toSeconds(time) + ' seconds';
  } else {
    timeStr = '';
  }
  console.log('done' + timeStr + '. Writing File...');
}

var shoppingList = [];
fs.writeFileSync('./combos.csv', combos.map(function (c) {
  shoppingList.push(c.recipe);
  c.toString();
}).join('\n'));

if (VERBOSE) {
  if (TIMED) {
    time = process.hrtime(last);
    last = process.hrtime();
    timeStr = ' in ' + toSeconds(time) + ' seconds';
  } else {
    timeStr = '';
  }
  console.log('done' + timeStr + '. Writing Shopping List...');
}

fs.writeFileSync('./shopping.csv', shoppingList.join('\n'));

if (TIMED) {
  console.log('Completed in ' + toSeconds(process.hrtime(START)) + ' seconds.');
}
process.exit(0);
