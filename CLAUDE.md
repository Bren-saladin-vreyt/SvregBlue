# SvregBlue: notes for Claude sessions

## The user
- Casual Indonesian ("bro"); answer the same way. Plays on a Poco X6 Pro (Zalith Launcher, MobileGlues), sometimes a low-end PC: keep it light.
- Wants features built one at a time, each behind an on/off toggle, tested on the phone before the next.
- Visual target: three blue-hour reference photos (misty tree with white flowers at night, ultramarine sea with white foam and moon glints, cyan foggy forest road).

## Project
- Fabric client mod, Minecraft 26.2, Java 25, package `com.svreg.blue`. Companion of SvregSky (`Bren-saladin-vreyt/SvregSky`, read its CLAUDE.md for 26.2 render API patterns and phone GLES gotchas).
- Gradle can't download in the Claude workspace: build only through GitHub CI (push to `main` -> `latest` release).

## Code map
- `look/BlueTime.java`: blue amount from the overworld clock + mode (dusk_night / night / always) x strength.
- `look/BlueGrade.java`: scene copy + `shaders/core/blue_grade.fsh` (luminance -> navy/ultramarine/cyan/ice ramp, navy lift, warm-light keep). Also `tintFog()`.
- Hooks: with SvregSky -> `@Pseudo` mixin on `com.svreg.sky.look.PostLook.render()` HEAD (grade runs under SvregSky's looks); without -> `CloudRenderer.render` HEAD. Clouds are drawn after the grade (not graded).
- `look/BlueFog.java` + `FogSetupMixin` (Kabut Biru): `AtmosphericFogEnvironment.setupFog` TAIL pulls `FogData.environmentalStart/End` in (density 100% = 6..44 blocks), follows `BlueTime.amount()`, eases to 35% indoors/caves (`canSeeSky`). Distances only, colour comes from `tintFog`.
- `FogColorMixin` (priority 1100) tints `AtmosphericFogEnvironment.getBaseColor` after SvregSky's own tint.
- Config `config/svregblue.json` (`config/BlueConfig.java`), menu `config/BlueScreen.java`, button under SvregSky's in Options.
- Validate shaders with SvregSky's `tools/glsl/check.js`.

## CI probe
- `probe.yml` (manual, input `pattern` = path regex under net/minecraft) decompiles 26.2 and pushes the matching sources to the `probe` branch (artifact downloads are blocked here). Run: `gh api -X POST repos/Bren-saladin-vreyt/SvregBlue/actions/workflows/probe.yml/dispatches -f ref=main -f 'inputs[pattern]=renderer/fog/'`, then `git fetch origin probe:refs/remotes/origin/probe -f && git archive refs/remotes/origin/probe | tar -x -C <dir>`.
