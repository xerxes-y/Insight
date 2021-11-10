import React, {useEffect} from 'react';
import {getUser, removeUserSession} from './Utils/Common';
import CssBaseline from '@material-ui/core/CssBaseline'
import EnhancedTable from './components/EnhancedTable'
import axios from "axios";

function Dashboard(props) {

  const user = getUser();

  // handle click event of logout button
  const handleLogout = () => {
    removeUserSession();
    props.history.push('/login');
  }

  const columns = React.useMemo(
    () => [
      {
        Header: 'Status Id',
        accessor: 'id',

      },
      {
        Header: 'Service Name',
        accessor: 'name',
      },
      {
        Header: 'Service Id',
        accessor: 'serviceId',
      },
      {
        Header: 'URL',
        accessor: 'url',
      },
      {
        Header: 'Status',
        accessor: 'status',
      },
      {
        Header: 'User',
        accessor: 'user',
      },
      {
        Header: 'Time',
        accessor: 'createdAt',
      },
    ],
    []
  )

  const [data, setData] = React.useState([])
  const [skipPageReset, setSkipPageReset] = React.useState(false)
  const [shouldFetchAgain, setShouldFetchAgain] = React.useState(true)

  const fetchServices = async () => {
    setShouldFetchAgain(false)
    axios.get(`http://localhost:8888/services-user/${user}`)
      .catch((e) => {
        // handle error
        console.log(e)
      }).then(res => {
          setData(res.data)
        }
      )


  }

  useEffect(() => {
    if (!shouldFetchAgain) return;
    fetchServices()

    return () => {
      setTimeout(() => {
        setShouldFetchAgain(true)
      }, 1000)
    }
  }, [shouldFetchAgain])
  // We need to keep the table from resetting the pageIndex when we
  // Update data. So we can keep track of that flag with a ref.

  // When our cell renderer calls updateMyData, we'll use
  // the rowIndex, columnId and new value to update the
  // original data
  const updateMyData = async (rowIndex, columnId, value) => {
    console.log(rowIndex, columnId, value)
    const updatedItem = {
      ...data[rowIndex],
      [columnId]: value
    }
   await axios.put(`http://localhost:8888/services/${updatedItem.serviceId}`, {
      service: updatedItem
    }).catch(reason => console.log(reason)).then(newVaue => {
      setSkipPageReset(true)
      setData(old => {
          console.log(old)
            const newSet = old.map((row, index) => {
              if (index === rowIndex) {
                return updatedItem
              }
              return row
            })
            console.log('NEW SET', newSet)
            return newSet


        }
      )
    })
  }


    return (
      <div>
        <div>
          Welcome {user.name}!<br/><br/>
          <input type="button" onClick={handleLogout} value="Logout"/>
        </div>
        <CssBaseline/>
        <EnhancedTable
          columns={columns}
          data={data}
          setData={setData}
          updateMyData={updateMyData}
          skipPageReset={skipPageReset}
        />
      </div>
    )
  }

export default Dashboard
