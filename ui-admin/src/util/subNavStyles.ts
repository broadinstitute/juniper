/** Style helpers for subnavigation on a page -- e.g. a side navigation bar inside the site's primary sidenav bar */

/** style for a list item in a subnav */
export const navListItemStyle = {
  backgroundColor: '#ededed',
  marginBottom: '0.25em',
  padding: '0.5em'
}


/** dynamic style for nav links to indicate active route */
export  const navLinkStyleFunc = ({ isActive }: {isActive: boolean}) => {
  return isActive ? { fontWeight: 'bold' } : {}
}

/** style for containing div of the whole subnav menu */
export const navDivStyle = { minWidth: '290px', maxWidth: '290px' }

export const tabLinkStyle = ({ isActive }: {isActive: boolean}) => ({
  borderBottom: isActive ? '2px solid #708DBC': '',
  background: isActive ? '#E1E8F7' : ''
})
