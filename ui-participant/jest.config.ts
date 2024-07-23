export default {
  preset: 'ts-jest/presets/default-esm',
  testEnvironment: 'jsdom',
  extensionsToTreatAsEsm: ['.ts'],
  transform: {
    '^.+\\.(tsx|ts|js|jsx)?$': ['babel-jest']
    // process `*.tsx` files with `ts-jest`
  },
  setupFilesAfterEnv: ['@testing-library/jest-dom', '<rootDir>../ui-core/src/test-utils/setupTests.ts'],
  moduleDirectories: ['node_modules', 'src'],
  moduleNameMapper: {
    '\\.(gif|ttf|eot|svg|png|css|scss)$': '<rootDir>/../ui-core/src/test-utils/mockFile.js',
    'react-markdown': '<rootDir>/../node_modules/react-markdown/react-markdown.min.js',
    'micromark': '<rootDir>/../ui-core/src/test-utils/micromark-mock.js',
    '@juniper/ui-core': '<rootDir>/../ui-core/src/index.ts',
    'authConfig': '<rootDir>/../ui-core/src/test-utils/mockAuthConfig.js',
    'util/envVars': '<rootDir>/../ui-core/src/test-utils/mockFile.js'
  }
}
