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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.cketti.mailto.EmailIntentBuilder
import kotlinx.android.synthetic.main.activity_main.*
import io.noties.markwon.Markwon



class MainActivity : AppCompatActivity() {
    private lateinit var markwon: Markwon


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        markwon = Markwon.create(this)

        setAboutText()
        setUsageText()

        sendFeedbackButton.setOnClickListener {
            sendFeedback()
        }
    }

    private fun setAboutText() {
        val version = BuildConfig.VERSION_NAME
        val author = getString(R.string.author)
        aboutText.text = getString(R.string.about_text_format, version, author)
    }

    private fun setUsageText() {
        val appName = getString(R.string.app_name)
        val pickContactActivityTitle = getString(R.string.pick_contact_activity_title)
        val markdownText = getString(R.string.usage_text_format, appName, pickContactActivityTitle)
        markwon.setMarkdown(usageText, markdownText)
    }

    private fun sendFeedback() {
        EmailIntentBuilder.from(this)
            .to(getString(R.string.feedback_email))
            .subject(getString(R.string.feedback_subject))
            .body(getString(R.string.feedback_placeholder))
            .start()
    }
}
