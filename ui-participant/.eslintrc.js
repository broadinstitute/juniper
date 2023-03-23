// Taken from https://github.com/DataBiosphere/terra-ui/blob/6ee212abfc572d75ba6e22b788cf11730219dbff/.eslintrc.js#L4

module.exports = {
  'env': {
    'browser': true,
    'es6': true
  },
  'extends': [
    'eslint:recommended',
    'plugin:react/recommended',
    'plugin:jest/recommended',
    'plugin:@typescript-eslint/eslint-recommended',
    'plugin:@typescript-eslint/recommended'
  ],
  'globals': {
    module: true
  },
  'parser': '@typescript-eslint/parser',
  'parserOptions': {
    'ecmaFeatures': {
      'jsx': true
    },
    'ecmaVersion': 2018,
    'sourceType': 'module'
  },
  'plugins': [
    'react',
    'jsx-a11y',
    'import',
    'jest'
  ],
  'rules': {
    'array-bracket-newline': ['error', 'consistent'],
    'array-bracket-spacing': 'error',
    'block-spacing': 'error',
    'brace-style': ['error', '1tbs', { 'allowSingleLine': true }],
    'camelcase': 'error',
    'comma-dangle': ['error', 'never'],
    'comma-spacing': 'error',
    'comma-style': 'error',
    'computed-property-spacing': 'error',
    'curly': ['error', 'all'],
    'eol-last': 'error',
    'func-call-spacing': 'error',
    'indent': ['error', 2, {
      'SwitchCase': 1, 'CallExpression': { 'arguments': 1 },
      'ignoredNodes': ['TemplateLiteral']
    }],
    'key-spacing': 'error',
    'keyword-spacing': 'error',
    'lines-between-class-members': 'error',
    'max-len': ['error', { 'code': 120 }],
    'multiline-comment-style': 'off',
    'no-lonely-if': 'off',
    'no-multi-assign': 'error',
    'no-multiple-empty-lines': 'error',
    'no-trailing-spaces': 'error',
    'no-unneeded-ternary': 'error',
    'no-whitespace-before-property': 'error',
    'nonblock-statement-body-position': 'error',
    'object-curly-newline': ['error', { 'multiline': true, 'consistent': true }],
    'object-curly-spacing': ['error', 'always'],
    'one-var': ['error', 'never'],
    'padded-blocks': ['error', 'never'],
    'quotes': ['error', 'single', { 'allowTemplateLiterals': true }],
    'require-jsdoc': 'error',
    'semi': ['error', 'never'],
    'space-before-blocks': 'error',
    'space-before-function-paren': ['error', { 'anonymous': 'never', 'named': 'never', 'asyncArrow': 'always' }],
    'space-in-parens': 'error',
    'valid-jsdoc': 'off',

    // ES6
    'arrow-parens': ['error', 'as-needed'],
    'arrow-spacing': 'error',
    'no-duplicate-imports': 'error',
    'no-useless-rename': 'error',
    'no-var': 'error',
    'object-shorthand': 'error',
    'prefer-arrow-callback': 'error',
    'prefer-const': 'error',
    'prefer-template': 'error',
    'prefer-rest-params': 'error',
    'prefer-spread': 'error',
    'rest-spread-spacing': 'error',
    'template-curly-spacing': 'off',
    // React
    'react/prop-types': 'off',
    'react/jsx-key': 'off',
    'react/jsx-curly-spacing': ['error', { 'when': 'never' }],
    'react/jsx-no-target-blank': 'off',
    //Jest
    'jest/no-disabled-tests': 'error',
    // remove .only from your tests whenever you are using the exclusivity feature
    // so test can be executed on build system
    'jest/no-focused-tests': 'error',
    'jest/no-identical-title': 'error',
    //Reminds should be used upon asserting expectations on object's length property
    'jest/prefer-to-have-length': 'error'
  }
}
