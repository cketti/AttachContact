/*
 * Copyright (C) 2012-2019 cketti
 * Portions Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package de.cketti.attachcontact

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

class PickContactActivity : FragmentActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            Intent.ACTION_GET_CONTENT -> {
                if (savedInstanceState == null) {
                    checkPermissionAndPickContact()
                }
            }
            else -> {
                // This shouldn't happen. If it does, we just finish the activity.
                finishWithError()
            }
        }
    }

    private fun checkPermissionAndPickContact() {
        if (hasContactPermission()) {
            pickContact()
        } else {
            requestContactPermissionOrShowRationale()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_CONTACT_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickContact()
                } else {
                    Toast.makeText(this, R.string.error_no_permission, Toast.LENGTH_LONG).show()
                    finishWithError()
                }
            }
        }
    }

    private fun pickContact() {
        val pickIntent = Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI)

        try {
            startActivityForResult(pickIntent, REQUEST_CODE_PICK_CONTACT)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_activity_not_found, Toast.LENGTH_LONG).show()
            finishWithError()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_PICK_CONTACT -> {
                if (resultCode != Activity.RESULT_OK) {
                    finishWithError()
                    return
                }

                try {
                    //TODO: better error handling
                    val resultIntent = handlePickedContact(data) ?: throw RuntimeException("resultIntent == null")
                    finishWithResult(resultIntent)
                } catch (e: Exception) {
                    Toast.makeText(this, R.string.error_processing_contact, Toast.LENGTH_LONG).show()

                    finishWithError()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handlePickedContact(pickResultIntent: Intent?): Intent? {
        val contactUri = pickResultIntent?.data ?: return null

        val cursor = contentResolver.query(contactUri, arrayOf(Contacts.LOOKUP_KEY), null, null, null)
        val lookup = cursor?.use {
            cursor.moveToFirst()
            cursor.getString(cursor.getColumnIndex(Contacts.LOOKUP_KEY))
        } ?: return null

        val vCardUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookup)

        return Intent().apply {
            data = vCardUri
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    }

    private fun hasContactPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, CONTACT_PERMISSION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactPermissionOrShowRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, CONTACT_PERMISSION)) {
            val dialogFragment = PermissionRationaleDialogFragment()
            dialogFragment.show(supportFragmentManager, TAG_PERMISSION_RATIONALE)
        } else {
            requestContactPermission()
        }
    }

    private fun requestContactPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(CONTACT_PERMISSION), REQUEST_CODE_CONTACT_PERMISSION)
    }

    private fun finishWithResult(resultIntent: Intent) {
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun finishWithError() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }


    class PermissionRationaleDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireContext()).apply {
                setTitle(R.string.pick_contact_activity_title)
                setMessage(R.string.permission_rationale)
                setPositiveButton(R.string.okay_action) { _, _ ->
                    val pickContactActivity = requireActivity() as PickContactActivity
                    pickContactActivity.requestContactPermission()
                }
            }.create()
        }
    }


    companion object {
        private const val REQUEST_CODE_PICK_CONTACT = 1
        private const val REQUEST_CODE_CONTACT_PERMISSION = 2

        private const val TAG_PERMISSION_RATIONALE = "rationale"

        private const val CONTACT_PERMISSION = Manifest.permission.READ_CONTACTS
    }
}
