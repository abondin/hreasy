module.exports = {
    root: true,

    env: {
        node: true
    },

    'extends': [
        'plugin:vue/essential',
        'eslint:recommended',
        '@vue/typescript',
        '@vue/typescript/recommended'
    ],

    parserOptions: {
        parser: '@typescript-eslint/parser',
        ecmaVersion: 11,
    },

    "overrides": [
        {
            "files": [
                "**/__tests__/*.{j,t}s?(x)",
                "**/tests/unit/**/*.spec.{j,t}s?(x)"
            ],
            "env": {
                "jest": true
            }
        }
    ],

    rules: {
        'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        // FIXME set me to false later (100500 changes in files)
        "vue/valid-v-slot": ["error", {
            "allowModifiers": true
        }]
    },
}
