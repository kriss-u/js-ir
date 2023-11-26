// function uppercaseValueInObject(obj, key) {
//     if (Object.prototype.hasOwnProperty.call(obj, key)) {
//         if (typeof obj[key] === "string") {
//             obj[key] = obj[key].toUpperCase();
//         }
//     }
//
// }

// var obj = {
//     a: "john",
//     b: "doe",
//     c: {
//         a: "mary",
//         b: "doe"
//     }
// }
//
// obj.a = "jane";


var state = {
    name: ""
}

function setState(newState) {
    state = Object.assign({}, state, newState);
}

function onClick() {
    var profile = {
        name: "john",
        age: 20
    }
    var nameOfUser = profile.name;
    setState({name: nameOfUser});
}

onClick();

