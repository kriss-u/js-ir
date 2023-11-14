function uppercaseValueInObject(obj, key) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
        if (typeof obj[key] === "string") {
            obj[key] = obj[key].toUpperCase();
        }
    }

}

var obj = {
    a: "john",
    b: "doe"
}

obj.a = 'jane';
obj.b = 'smith';

uppercaseValueInObject(obj, 'a');