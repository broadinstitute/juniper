import { useEffect } from 'react'


type DocumentTitleProps = {
    title?: string, // html document.title
    defaultSuffix?: boolean, // whether to append ' | Juniper' to the end of the title
    printTitle?: string  // optional print-specific title
}

/** changes the document title of the page.  By default adds ' | Juniper' as a suffix */
const DocumentTitle = (props: DocumentTitleProps) => {
  const { title, defaultSuffix=true, printTitle } = props
  let fullTitle = 'Juniper'
  if (defaultSuffix && title) {
    fullTitle = `${title} | Juniper`
  } else if (title) {
    fullTitle = title
  }
  const beforePrintHandler = () => {
    document.title = printTitle ?? fullTitle
  }
  const afterPrintHandler = () => {
    document.title = fullTitle
  }

  useEffect(() => {
    const previousTitle = document.title
    document.title = fullTitle
    window.addEventListener('beforeprint', beforePrintHandler)
    window.addEventListener('afterprint', afterPrintHandler)

    return () => {
      document.title = previousTitle
      window.removeEventListener('beforeprint', beforePrintHandler)
      window.removeEventListener('afterprint', beforePrintHandler)
    }
  }, [fullTitle, printTitle])

  return null
}

export default DocumentTitle
