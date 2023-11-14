// function uppercaseValueInObject(obj, key) {
//     if (Object.prototype.hasOwnProperty.call(obj, key)) {
//         if (typeof obj[key] === "string") {
//             obj[key] = obj[key].toUpperCase();
//         }
//     }
//
// }

var obj = {
    a: "john",
    b: "doe",
    c: {
        "a": "mary",
        "b": "doe"
    }
}

obj.a = "jane";