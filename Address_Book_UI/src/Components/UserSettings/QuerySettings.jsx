import { useState, useContext } from "react"
import UserContext from "../../Context/UserProvider";
import SearchBar from "../SearchBar";


function QuerySettings() {
    const [option, setOption] = useState(0);
    const [search, setSearch] = useState("");
    const { role } = useContext(UserContext)

    return (
        <div className="main-container"
            style={{ width: "1000px" }}>
            <div className="main-header-container">
                <h2 className='main-header-text'>
                    Queries
                </h2>
            </div>
            <hr style={{ marginTop: "20px" }} />
            <div className="query-container">
                <form>
                    <div>
                        <select
                            name='option'
                            value={option}
                            onChange={event => setOption(event.target.value)}>
                            <option value="0">Records with the most common labels.</option>
                            <option value="1">All records with the same first name and different last names.</option>
                            <option value="2">All records with different first names and the same last name.</option>
                            <option value="3">Record with a specific first and last name.</option>
                            {(role == "ADMIN") && <option value="4">Get all records.</option>}
                        </select>
                        <button
                            className="small-button"
                            style={{ backgroundColor: "rgb(191, 244, 174)" }}
                            type="button"
                        // onClick={() => handleDelete(index)}
                        >
                            ✓
                        </button>
                    </div>
                    {option == "3" && <div>
                        <SearchBar
                            search={search}
                            setSearch={setSearch}
                            placeholder={"Search Name"} />
                    </div>}
                </form>
            </div>
        </div>
    )
}

export default QuerySettings