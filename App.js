var _interopRequireDefault = require('@babel/runtime/helpers/interopRequireDefault');
Object.defineProperty(exports, '__esModule', {value: true});
exports.default = App;
var _defineProperty2 = _interopRequireDefault(
    require('@babel/runtime/helpers/defineProperty'),
);
var _slicedToArray2 = _interopRequireDefault(
    require('@babel/runtime/helpers/slicedToArray'),
);
require('expo-dev-client');
var _react = _interopRequireWildcard(require('react'));
var _reactNative = require('react-native');
var _jsxRuntime = require('react/jsx-runtime');

function _getRequireWildcardCache(e) {
    if ('function' != typeof WeakMap) return null;
    var r = new WeakMap(),
        t = new WeakMap();
    return (_getRequireWildcardCache = function _getRequireWildcardCache(e) {
        return e ? t : r;
    })(e);
}

function _interopRequireWildcard(e, r) {
    if (!r && e && e.__esModule) return e;
    if (null === e || ('object' != typeof e && 'function' != typeof e))
        return {default: e};
    var t = _getRequireWildcardCache(r);
    if (t && t.has(e)) return t.get(e);
    var n = {__proto__: null},
        a = Object.defineProperty && Object.getOwnPropertyDescriptor;
    for (var u in e)
        if ('default' !== u && Object.prototype.hasOwnProperty.call(e, u)) {
            var i = a ? Object.getOwnPropertyDescriptor(e, u) : null;
            i && (i.get || i.set) ? Object.defineProperty(n, u, i) : (n[u] = e[u]);
        }
    return (n.default = e), t && t.set(e, n), n;
}

function ownKeys(e, r) {
    var t = Object.keys(e);
    if (Object.getOwnPropertySymbols) {
        var o = Object.getOwnPropertySymbols(e);
        r &&
        (o = o.filter(function (r) {
            return Object.getOwnPropertyDescriptor(e, r).enumerable;
        })),
            t.push.apply(t, o);
    }
    return t;
}

function _objectSpread(e) {
    for (var r = 1; r < arguments.length; r++) {
        var t = null != arguments[r] ? arguments[r] : {};
        r % 2
            ? ownKeys(Object(t), !0).forEach(function (r) {
                (0, _defineProperty2.default)(e, r, t[r]);
            })
            : Object.getOwnPropertyDescriptors
                ? Object.defineProperties(e, Object.getOwnPropertyDescriptors(t))
                : ownKeys(Object(t)).forEach(function (r) {
                    Object.defineProperty(e, r, Object.getOwnPropertyDescriptor(t, r));
                });
    }
    return e;
}

function App() {
    var _useState = (0, _react.useState)({username: '', password: ''}),
        _useState2 = (0, _slicedToArray2.default)(_useState, 2),
        form = _useState2[0],
        setForm = _useState2[1];
    var _useState3 = (0, _react.useState)(false),
        _useState4 = (0, _slicedToArray2.default)(_useState3, 2),
        loading = _useState4[0],
        setLoading = _useState4[1];
    var handleChange = (0, _react.useCallback)(function (fieldName) {
        return function (value) {
            return setForm(function (form) {
                return _objectSpread(
                    _objectSpread({}, form),
                    {},
                    (0, _defineProperty2.default)({}, fieldName, value),
                );
            });
        };
    }, []);
    var handleSubmit = (0, _react.useCallback)(
        function () {
            console.log('Submitting form: ', form);
            setLoading(true);
            return new Promise(function (resolve, reject) {
                setTimeout(function () {
                    resolve('Simulated data from the server');
                    if (Math.random() * 10 < 1) reject(new Error('Simulated error'));
                    setLoading(false);
                }, 1500);
            });
        },
        [form],
    );
    return (0, _jsxRuntime.jsxs)(_reactNative.View, {
        style: styles.container,
        children: [
            (0, _jsxRuntime.jsx)(_reactNative.TextInput, {
                placeholder: 'Username',
                value: form.username,
                style: styles.textInput,
                onChangeText: handleChange('username'),
            }),
            (0, _jsxRuntime.jsx)(_reactNative.TextInput, {
                placeholder: 'Password',
                value: form.password,
                style: styles.textInput,
                onChangeText: handleChange('password'),
                secureTextEntry: true,
            }),
            (0, _jsxRuntime.jsx)(_reactNative.TouchableOpacity, {
                style: loading ? styles.buttonDisabled : styles.button,
                onPress: handleSubmit,
                disabled: loading,
                children: (0, _jsxRuntime.jsx)(_reactNative.Text, {
                    children: 'Login',
                }),
            }),
        ],
    });
}

var styles = _reactNative.StyleSheet.create({
    container: {
        flex: 1,
        padding: 16,
        backgroundColor: '#fff',
        alignItems: 'center',
        justifyContent: 'center',
    },
    textInput: {
        height: 40,
        borderColor: 'gray',
        borderWidth: 1,
        borderRadius: 4,
        width: '100%',
        paddingHorizontal: 10,
        marginBottom: 10,
    },
    button: {
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 12,
        paddingHorizontal: 32,
        borderRadius: 4,
        elevation: 3,
        backgroundColor: '#2196F3',
        width: '100%',
        marginBottom: 10,
    },
    buttonDisabled: {
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 12,
        paddingHorizontal: 32,
        borderRadius: 4,
        elevation: 3,
        backgroundColor: '#ccc',
        width: '100%',
        marginBottom: 10,
    },
});
