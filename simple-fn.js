var a = 1;
var b = 2;
var aMultiplier = 1.5;
var bMultiplier = 2;
var result = 100;

function onClick() {
    a = b;
    b = 20 + a;
    var result = (a * aMultiplier) + (b * bMultiplier);
}

