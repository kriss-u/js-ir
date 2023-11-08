/*
Original code before babel:

function sum(...args) {
    return args.reduce((acc, i) => {
        acc += i;
        return acc;
    }, 0);
}

console.log(sum(1, 2, 10, 20));

*/

/*
 */
function sum() {
    for (
        var _len = arguments.length, args = new Array(_len), _key = 0;
        _key < _len;
        _key++
    ) {
        args[_key] = arguments[_key];
    }
    return args.reduce(function (acc, i) {
        acc += i;
        return acc;
    }, 0);
}

console.log(sum(1, 2, 10, 20));




