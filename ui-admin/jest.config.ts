export default {
  preset: 'ts-jest/presets/default-esm',
  testEnvironment: 'jsdom',
  extensionsToTreatAsEsm: ['.ts'],
  transform: {
    '^.+\\.(tsx|ts|js|jsx)?$': ['babel-jest']
    // process `*.tsx` files with `ts-jest`
  },
  setupFilesAfterEnv: ['@testing-library/jest-dom/extend-expect'],
  moduleDirectories: ['node_modules', 'src'],
  moduleNameMapper: {
    '\\.(gif|ttf|eot|svg|png|css)$': '<rootDir>/../ui-core/src/test-utils/mockFile.js',
    'react-markdown': '<rootDir>/../node_modules/react-markdown/react-markdown.min.js',
    'micromark': '<rootDir>/../ui-core/src/test-utils/micromark-mock.js',
    '@juniper/ui-core': '<rootDir>/../ui-core/build/es/index.js'
  }
}
