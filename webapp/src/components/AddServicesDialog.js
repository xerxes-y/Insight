import React, { useState } from 'react'

import AddIcon from '@material-ui/icons/Add'
import Button from '@material-ui/core/Button'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogTitle from '@material-ui/core/DialogTitle'
import IconButton from '@material-ui/core/IconButton'
import PropTypes from 'prop-types'
import Switch from '@material-ui/core/Switch'
import TextField from '@material-ui/core/TextField'
import Tooltip from '@material-ui/core/Tooltip'
import {getUser} from "../Utils/Common";

const initialService = {
  user: getUser(),
  url: '',
  name: ''
}

const AddServiceDialog = props => {
  const [service, setService] = useState(initialService)
  const { addServicesHandler } = props
  const [open, setOpen] = React.useState(false)

  const [switchState, setSwitchState] = React.useState({
    addMultiple: false,
  })

  const handleSwitchChange = name => event => {
    setSwitchState({ ...switchState, [name]: event.target.checked })
  }

  const resetSwitch = () => {
    setSwitchState({ addMultiple: false })
  }

  const handleClickOpen = () => {
    setOpen(true)
  }

  const handleClose = () => {
    setOpen(false)
    resetSwitch()
  }

  const handleAdd = event => {
    addServicesHandler(service)
    setService(initialService)
    switchState.addMultiple ? setOpen(true) : setOpen(false)
  }
  const handleInputChange = (e) => {
    setService({
      ...service,
      [e.target.name]: e.target.value
    })
  }
  return (
    <div>
      <Tooltip title="Add">
        <IconButton aria-label="add" onClick={handleClickOpen}>
          <AddIcon />
        </IconButton>
      </Tooltip>
      <Dialog
        open={open}
        onClose={handleClose}
        aria-labelledby="form-dialog-title">
        <DialogTitle id="form-dialog-title">Add Service</DialogTitle>
        <DialogContent>
          <DialogContentText>Please Add Your Services.</DialogContentText>
          <TextField
            autoFocus
            margin="dense"
            label="Service Name"
            type="text"
            name="name"
            fullWidth
            value={service.name}
            onChange={handleInputChange}
          />
          <TextField
            margin="dense"
            label="URL"
            name="url"
            type="text"
            fullWidth
            value={service.url}
            onChange={handleInputChange}
          />
        </DialogContent>
        <DialogActions>
          <Tooltip title="Add multiple">
            <Switch
              checked={switchState.addMultiple}
              onChange={handleSwitchChange('addMultiple')}
              value="addMultiple"
              inputProps={{ 'aria-label': 'secondary checkbox' }}
            />
          </Tooltip>
          <Button onClick={handleClose} color="primary">
            Cancel
          </Button>
          <Button onClick={handleAdd} color="primary">
            Add
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  )
}

AddServiceDialog.propTypes = {
  addServicesHandler: PropTypes.func.isRequired,
}

export default AddServiceDialog
