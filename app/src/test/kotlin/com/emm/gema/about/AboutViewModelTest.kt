package com.emm.gema.about

import app.cash.turbine.test
import com.emm.gema.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private const val VERSION_NAME: String = "1.0"

class AboutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel: AboutViewModel = AboutViewModel(appVersion = FakeAppVersionProvider(VERSION_NAME))

    @Test
    fun `state carries the installed version name`() {
        assertThat(viewModel.state.value.version).isEqualTo(VERSION_NAME)
    }

    @Test
    fun `source link clicked opens that source's url`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(AboutUiIntent.SourceLinkClicked(AboutSource.SIAGIE))

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.OpenUrl(AboutSource.SIAGIE.url))
        }
    }

    @Test
    fun `back clicked navigates back`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(AboutUiIntent.BackClicked)

            assertThat(awaitItem()).isEqualTo(AboutUiEffect.NavigateBack)
        }
    }
}

private class FakeAppVersionProvider(private val version: String) : AppVersionProvider {
    override fun versionName(): String = version
}
