const SearchContact = ({ search, setSearch }) => {
    return (
        <form className='searchForm' onSubmit={(e) => e.preventDefault()}>
            <input
                id='search'
                type='text'
                role='searchbox'
                placeholder='Search Contacts'
                value={search}
                onChange={(e) => setSearch(e.target.value)}
            />
        </form>
    )
}

export default SearchContact