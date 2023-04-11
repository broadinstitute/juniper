/* eslint-disable-next-line */
const {createProxyMiddleware} = require('http-proxy-middleware')

module.exports = function (app) {
  app.use(
    ['/api', '/config', '/favicon.ico'],
    createProxyMiddleware({
      target: 'http://localhost:8081'
    })
  )
}
