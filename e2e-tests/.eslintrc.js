// Taken from https://github.com/DataBiosphere/terra-ui/blob/6ee212abfc572d75ba6e22b788cf11730219dbff/.eslintrc.js#L4
/* eslint-env node */

const isFix = process.argv.includes('--fix')

module.exports = {
  'root': true,
  extends: [
    '../.eslintrc.js'
  ],
  parserOptions: {
    project: 'tsconfig.json',
    tsconfigRootDir: __dirname
  },
  rules: {
    // ESLint thinks the Playwright test function is jest's objects to its parameters.
    'jest/no-done-callback': 'off',
    'jest/expect-expect': 'off',
    'jest/no-standalone-expect': 'off',
    'jest/no-conditional-expect': 'off',
    'jest/valid-expect': 'off',
    '@typescript-eslint/promise-function-async': 'error',
    '@typescript-eslint/no-floating-promises': 'error',
    '@typescript-eslint/no-misused-promises': 'error',
    'require-await': 'off', // Note: disable the base rule for @typescript-eslint/require-await
    '@typescript-eslint/require-await': 'error'
  }
}
