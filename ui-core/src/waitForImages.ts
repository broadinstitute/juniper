/** returns a promise that resolves when all images on the page are finished loading */
export const waitForImages = () => new Promise(resolve => {
  const images = Array.from(document.querySelectorAll('img'))
  if (images.length === 0) {
    resolve(undefined)
    return
  }

  let nComplete = 0

  const cb = () => {
    nComplete += 1
    if (nComplete === images.length) {
      resolve(undefined)
    }
  }

  for (const image of images) {
    const clone = new Image()
    clone.onload = cb
    clone.onerror = cb
    clone.src = image.src
  }
})
