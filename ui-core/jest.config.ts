export default {
  preset: 'ts-jest/presets/default-esm',
  testEnvironment: 'jsdom',
  extensionsToTreatAsEsm: ['.ts'],
  transform: {
    '^.+\\.(tsx|ts|js|jsx)?$': ['babel-jest']
    // process `*.tsx` files with `ts-jest`
  },
  setupFilesAfterEnv: ['@testing-library/jest-dom', '<rootDir>/src/test-utils/setupTests.ts'],
  moduleDirectories: ['node_modules', '<rootDir>'],
  modulePaths: ['<rootDir>/src'],
  moduleNameMapper: {
    '\\.(gif|ttf|eot|svg|png|css|scss)$': '<rootDir>/src/test-utils/mockFile.js',
    'react-markdown': '<rootDir>/../node_modules/react-markdown/react-markdown.min.js',
    'micromark': '<rootDir>/src/test-utils/micromark-mock.js',
    'authConfig': '<rootDir>/src/test-utils/mockFile.js'
  }
}
