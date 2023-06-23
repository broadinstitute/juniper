// Taken from https://github.com/DataBiosphere/terra-ui/blob/6ee212abfc572d75ba6e22b788cf11730219dbff/.eslintrc.js#L4

module.exports = {
  env: {
    browser: true,
    es6: true
  },
  extends: [
    'eslint:recommended',
    'plugin:react/recommended',
    'plugin:import/recommended',
    'plugin:import/typescript',
    'plugin:jest/recommended',
    'plugin:@typescript-eslint/eslint-recommended',
    'plugin:@typescript-eslint/recommended'
  ],
  globals: {
    module: true
  },
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaFeatures: {
      jsx: true
    },
    ecmaVersion: 2018,
    sourceType: 'module'
  },
  plugins: ['react', 'jsx-a11y', 'import', 'jest', 'jsdoc'],
  rules: {
    'array-bracket-newline': ['warn', 'consistent'],
    'array-bracket-spacing': 'warn',
    'block-spacing': 'warn',
    'brace-style': ['warn', '1tbs', { allowSingleLine: true }],
    camelcase: 'warn',
    'comma-dangle': ['error', 'never'],
    'comma-spacing': 'warn',
    'comma-style': 'warn',
    'computed-property-spacing': 'warn',
    curly: ['warn', 'all'],
    'eol-last': 'warn',
    'func-call-spacing': 'warn',
    indent: [
      'warn',
      2,
      {
        SwitchCase: 1,
        CallExpression: { arguments: 1 },
        ignoredNodes: ['TemplateLiteral']
      }
    ],
    'key-spacing': 'warn',
    'keyword-spacing': 'warn',
    'lines-between-class-members': 'warn',
    'max-len': ['error', { code: 120 }],
    'multiline-comment-style': 'off',
    'no-lonely-if': 'off',
    'no-multi-assign': 'warn',
    'no-multiple-empty-lines': 'warn',
    'no-trailing-spaces': 'warn',
    'no-unneeded-ternary': 'warn',
    'no-whitespace-before-property': 'warn',
    'nonblock-statement-body-position': 'warn',
    'object-curly-newline': ['warn', { multiline: true, consistent: true }],
    'object-curly-spacing': ['warn', 'always'],
    'one-var': ['warn', 'never'],
    'padded-blocks': ['warn', 'never'],
    quotes: ['warn', 'single', { allowTemplateLiterals: true }],
    semi: ['warn', 'never'],
    'space-before-blocks': 'warn',
    'space-before-function-paren': [
      'warn',
      { anonymous: 'never', named: 'never', asyncArrow: 'always' }
    ],
    'space-in-parens': 'warn',
    'valid-jsdoc': 'off',

    // ES6
    'arrow-parens': ['warn', 'as-needed'],
    'arrow-spacing': 'warn',
    'no-duplicate-imports': 'warn',
    'no-useless-rename': 'warn',
    'no-var': 'warn',
    'object-shorthand': 'warn',
    'prefer-arrow-callback': 'warn',
    'prefer-const': 'warn',
    'prefer-template': 'warn',
    'prefer-rest-params': 'warn',
    'prefer-spread': 'warn',
    'rest-spread-spacing': 'warn',
    'template-curly-spacing': 'off',
    // React
    'react/prop-types': 'off',
    'react/jsx-key': 'off',
    'react/jsx-curly-spacing': ['error', { when: 'never' }],
    'react/jsx-no-target-blank': 'off',
    //Jest
    'jest/no-disabled-tests': 'warn',
    // remove .only from your tests whenever you are using the exclusivity feature
    // so test can be executed on build system
    'jest/no-focused-tests': 'error',
    'jest/no-identical-title': 'error',
    //Reminds should be used upon asserting expectations on object's length property
    'jest/prefer-to-have-length': 'warn',

    // TypeScript
    '@typescript-eslint/ban-ts-comment': 'off',

    // JSDoc
    'jsdoc/require-jsdoc': ['warn', {
      publicOnly: true,
      require: {
        FunctionDeclaration: true,
        FunctionExpression: true,
        ArrowFunctionExpression: true
      }
    }]
  },
  overrides: [
    {
      files: 'e2e-tests/**/*.spec.ts',
      rules: {
        // ESLint thinks the Playwright test function is jest's objects to its parameters.
        'jest/no-done-callback': 'off'
      }
    }
  ],
  settings: {
    'import/resolver': {
      node: true,
      typescript: {
        project: [
          'ui-admin/tsconfig.json',
          'ui-core/tsconfig.json',
          'ui-participant/tsconfig.json'
        ]
      }
    }
  }
}
