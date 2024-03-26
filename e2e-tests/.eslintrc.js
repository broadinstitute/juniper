module.exports = {
  root: true,
  extends: [
    '../.eslintrc.js'
  ],
  plugins: ['@typescript-eslint'],
  overrides: [
    {
      files: 'src/**/*.ts',
      parserOptions: {
        project: './tsconfig.json'
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
        '@typescript-eslint/no-misused-promises': 'error'
      }
    }
  ]
}
