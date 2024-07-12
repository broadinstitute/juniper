export default {
    preset: 'ts-jest',
    testEnvironment: 'jest-environment-jsdom',
    transform: {
        '^.+\\.tsx?$': 'ts-jest'
        // process `*.tsx` files with `ts-jest`
    },
    setupFilesAfterEnv: ['@testing-library/jest-dom/extend-expect'],
    modulePaths: ['src'],
    moduleNameMapper: {
        '\\.(gif|ttf|eot|svg|png|css)$': '<rootDir>/../ui-core/src/test-utils/mockFile.js',
        'react-markdown': '<rootDir>/../node_modules/react-markdown/react-markdown.min.js',
        'micromark': '<rootDir>/../ui-core/src/test-utils/micromark-mock.js'
    }
};
