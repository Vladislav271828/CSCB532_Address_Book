const SearchBar = ({ search, setSearch, placeholder }) => {
    return (
        <form className='search-form' onSubmit={(e) => e.preventDefault()}>
            <input
                id='search'
                type='text'
                role='searchbox'
                placeholder={placeholder}
                value={search}
                onChange={(e) => setSearch(e.target.value)}
            />
        </form>
    )
}

export default SearchBar